package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TemporalPlanStateManager implements PlanStateManager { // TODO: Unit tests

    private final static Comparator<TemporalPlanAction> endStartTimeComparator = (t1, t2) -> new CompareToBuilder()
            .append(t1.getStartTimestamp(), t2.getStartTimestamp()).append(t1.getEndTimestamp(), t2.getEndTimestamp())
            .toComparison();
    private final Domain domain;
    private final Problem problem;
    private final List<TemporalPlanAction> temporalPlanActions;
    private PlanActionPointer pointer;
    private PlanState state;
    private ActionCost time;

    public TemporalPlanStateManager(Domain domain, Problem problem, Plan plan) {
        this.domain = domain;
        this.problem = problem;
        this.temporalPlanActions = plan.getTemporalPlanActions().stream().sorted(endStartTimeComparator)
                .collect(Collectors.toList());
        this.state = getOriginalState();
        this.time = ActionCost.valueOf(0);
        this.pointer = new PlanActionPointer(0, false);
    }

    @Override
    public PlanState getCurrentPlanState() {
        return state;
    }

    @Override
    public ActionCost getCurrentTime() {
        return time;
    }

    private PlanState getOriginalState() {
        return new DefaultPlanState(domain, problem);
    }

    @Override
    public void goToTime(ActionCost time) {
        state = getOriginalState();
        int simulationTime = applyAll(t -> t.getStartTimestamp() < time.getCost());
        pointer = new PlanActionPointer(simulationTime + 1, false);
    }

    @Override
    public void goToTimeRightAfter(ActionCost time) {
        state = getOriginalState();
        int simulationTime = applyAll(t -> t.getStartTimestamp() <= time.getCost());
        pointer = new PlanActionPointer(simulationTime, true);
    }

    private int applyAll(Predicate<TemporalPlanAction> filter) {
        PriorityQueue<IntQueueElement<TemporalPlanAction>> applyQueue = new PriorityQueue<>(temporalPlanActions.stream()
                .filter(filter)
                .map(t -> new IntQueueElement<>(t.getStartTimestamp(), t)).collect(Collectors.toList()));
        int simulationTime = 0;
        IntQueueElement<TemporalPlanAction> head;
        while ((head = applyQueue.poll()) != null) {
            TemporalPlanAction action = head.getPayload();
            if (simulationTime <= action.getStartTimestamp()) {
                state.applyPreconditions(action.getAction());
                applyQueue.add(new IntQueueElement<>(action.getEndTimestamp(), action));
            } else if (simulationTime <= action.getEndTimestamp()) {
                state.applyEffects(action.getAction());
            } else {
                throw new IllegalStateException("Cannot occur.");
            }
            simulationTime = head.getPriority();
        }
        return simulationTime;
    }

    @Override
    public void goToNextCheckpoint() {
        if (pointer.isPreconditionsApplied()) {
            temporalPlanActions.stream().flatMapToInt(t -> IntStream.of(t.getStartTimestamp(), t.getEndTimestamp()))
                    .filter(t -> t > time.getCost()).min().ifPresent(res -> goToTime(ActionCost.valueOf(res)));
        } else {
            goToTimeRightAfter(getCurrentTime());
        }
    }

    @Override
    public void goToPreviousCheckpoint() {
        if (pointer.isPreconditionsApplied()) {
            goToTime(getCurrentTime());
        } else {
            temporalPlanActions.stream().flatMapToInt(t -> IntStream.of(t.getStartTimestamp(), t.getEndTimestamp()))
                    .filter(t -> t <= time.getCost()).max().ifPresent(res -> goToTime(ActionCost.valueOf(res)));
        }
    }

    private static class IntQueueElement<Payload> implements Comparable<IntQueueElement<Payload>> {

        private final Payload payload;
        private final int priority;

        public IntQueueElement(int priority, Payload payload) {
            this.payload = payload;
            this.priority = priority;
        }

        /**
         * Get the payload.
         *
         * @return the payload
         */
        public Payload getPayload() {
            return payload;
        }

        /**
         * Get the index.
         *
         * @return the index
         */
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof IntQueueElement<?>)) {
                return false;
            }
            IntQueueElement<?> that = (IntQueueElement<?>) o;

            return new EqualsBuilder().append(getPriority(), that.getPriority()).append(getPayload(), that.getPayload())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getPriority()).append(getPayload()).toHashCode();
        }

        @Override
        public int compareTo(IntQueueElement<Payload> o) {
            return new CompareToBuilder().append(getPriority(), o.getPriority()).toComparison();
        }
    }
}
