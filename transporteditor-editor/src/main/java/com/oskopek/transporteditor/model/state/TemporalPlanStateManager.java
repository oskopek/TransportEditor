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
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A plan state manager for temporal domains. Can be used for sequential domains too.
 * Checkpoints are regarded as "time steps". Every integral time unit is divided into two time steps.
 */
public class TemporalPlanStateManager implements PlanStateManager {

    private static final Comparator<TemporalPlanAction> endStartTimeComparator = (t1, t2) -> new CompareToBuilder()
            .append(t1.getStartTimestamp(), t2.getStartTimestamp()).append(t1.getEndTimestamp(), t2.getEndTimestamp())
            .toComparison();
    private final Domain domain;
    private final Problem problem;
    private final List<TemporalPlanAction> temporalPlanActions;
    private PlanActionPointer pointer;
    private PlanState state;

    public TemporalPlanStateManager(Domain domain, Problem problem, Plan plan) {
        this.domain = domain;
        this.problem = problem;
        this.temporalPlanActions = plan.getTemporalPlanActions().stream().sorted(endStartTimeComparator)
                .collect(Collectors.toList());
        this.state = getBeginningState();
        this.pointer = new PlanActionPointer(0, false);
    }

    @Override
    public PlanState getCurrentPlanState() {
        return state;
    }

    @Override
    public ActionCost getCurrentTime() {
        return ActionCost.valueOf(pointer.getTime());
    }

    /**
     * Create a new plan state to symbolize the beginning state.
     *
     * @return the beginning state
     */
    private PlanState getBeginningState() {
        return new DefaultPlanState(domain, problem);
    }

    @Override
    public void goToTime(ActionCost time, boolean applyStarts) {
        state = getBeginningState();
        int simulationTime = applyAll(time.getCost(), applyStarts);
        pointer = new PlanActionPointer(simulationTime, applyStarts);
    }

    private int applyAll(int upToIncluding, boolean applyStarts) {
        Stream<TimeElement<TemporalPlanAction>> actions = temporalPlanActions.stream()
                .flatMap(t -> Stream.of(new TimeElement<>(t.getStartTimestamp(), false, t),
                        new TimeElement<>(t.getEndTimestamp(), true, t)))
                .filter(t -> t.getPriority() <= upToIncluding);

        if (!applyStarts) {
            actions = actions.filter(t -> !(t.getPriority() == upToIncluding && !t.isEnd()));
        }

        PriorityQueue<TimeElement<TemporalPlanAction>> applyQueue = new PriorityQueue<>(
                actions.collect(Collectors.toList()));

        int simulationTime = 0;
        TimeElement<TemporalPlanAction> head;
        while ((head = applyQueue.poll()) != null) {
            TemporalPlanAction action = head.getPayload();
            simulationTime = head.getPriority();
            if (simulationTime == action.getStartTimestamp()) {
                state.applyPreconditions(action.getAction());
            } else if (simulationTime == action.getEndTimestamp()) {
                state.applyEffects(action.getAction());
            } else {
                throw new IllegalStateException("Cannot occur.");
            }

        }
        return simulationTime;
    }

    @Override
    public void goToNextCheckpoint() {
        if (pointer.isStartsApplied()) {
            temporalPlanActions.stream().flatMapToInt(t -> IntStream.of(t.getStartTimestamp(), t.getEndTimestamp()))
                    .filter(t -> t > pointer.getTime()).min().ifPresent(res -> goToTime(ActionCost.valueOf(res),
                    false));
        } else {
            goToTime(getCurrentTime(), true);
        }
    }

    @Override
    public void goToPreviousCheckpoint() {
        if (pointer.isStartsApplied()) {
            goToTime(getCurrentTime(), false);
        } else {
            temporalPlanActions.stream().flatMapToInt(t -> IntStream.of(t.getStartTimestamp(), t.getEndTimestamp()))
                    .filter(t -> t < pointer.getTime()).max().ifPresent(res -> goToTime(ActionCost.valueOf(res), true));
        }
    }

    @Override
    public Optional<TemporalPlanAction> getLastAction() {
        return javaslang.collection.Stream.ofAll(temporalPlanActions).reverse()
                .dropWhile(t -> t.getStartTimestamp() > pointer.getTime())
                .dropWhile(t -> !pointer.isStartsApplied() && t.getStartTimestamp() == pointer.getTime())
                .take(1).toJavaOptional();
    }

    private static class TimeElement<Payload> implements Comparable<TimeElement<Payload>> {

        private final int priority;
        private final boolean isEnd;
        private final Payload payload;


        TimeElement(int priority, boolean isEnd, Payload payload) {
            this.payload = payload;
            this.isEnd = isEnd;
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

        public boolean isEnd() {
            return isEnd;
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
            if (o == null || !(o instanceof TimeElement<?>)) {
                return false;
            }
            TimeElement<?> that = (TimeElement<?>) o;

            return new EqualsBuilder().append(getPriority(), that.getPriority())
                    .append(isEnd(), that.isEnd())
                    .append(getPayload(), that.getPayload()).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getPriority()).append(isEnd())
                    .append(getPayload()).toHashCode();
        }

        @Override
        public int compareTo(TimeElement<Payload> o) {
            return new CompareToBuilder().append(getPriority(), o.getPriority())
                    .append(o.isEnd(), isEnd()) // reversed on purpose
                    .toComparison();
        }
    }
}
