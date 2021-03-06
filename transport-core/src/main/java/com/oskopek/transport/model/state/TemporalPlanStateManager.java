package com.oskopek.transport.model.state;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * A plan state manager for temporal domains. Can be used for sequential domains too.
 * Checkpoints in this manager are defined as applying the end of the next/previous action in the plan with respect to
 * time.
 */
public class TemporalPlanStateManager implements PlanStateManager {

    private final Problem problem;
    private final List<TemporalPlanAction> temporalPlanActions;
    private PlanActionPointer pointer;
    private PlanState state;

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem (beginning state)
     * @param plan the plan to simulate
     */
    public TemporalPlanStateManager(Domain domain, Problem problem, Plan plan) {
        this.problem = problem;
        this.temporalPlanActions = plan.getTemporalPlanActions().stream().sorted().collect(Collectors.toList());
        this.state = getBeginningState();
        this.pointer = new PlanActionPointer(0, false);
    }

    @Override
    public PlanState getCurrentPlanState() {
        return state;
    }

    @Override
    public Double getCurrentTime() {
        return pointer.getTime();
    }

    /**
     * Create a new plan state to symbolize the beginning state.
     *
     * @return the beginning state
     */
    private PlanState getBeginningState() {
        return new DefaultPlanState(problem);
    }

    @Override
    public void goToTime(Double time, boolean applyStarts) {
        state = getBeginningState();
        double simulationTime = applyAll(time, applyStarts);
        pointer = new PlanActionPointer(time, applyStarts);
    }

    /**
     * Applies all actions from the beginning of the plan to the given time, including. If apply starts is true,
     * also applies the "at start" effects of actions at the given time. Mutates the internal plan state.
     *
     * @param upToIncluding the time up to which to apply actions from the plan, including
     * @param applyStarts if true, will apply the "at start" effects of actions at time {@code upToIncluding}
     * @return the last time an action effect was applied
     */
    private double applyAll(double upToIncluding, boolean applyStarts) {
        Stream<TimeElement<TemporalPlanAction>> actions = temporalPlanActions.stream()
                .flatMap(t -> Stream.of(new TimeElement<>(t.getStartTimestamp(), false, t),
                        new TimeElement<>(t.getEndTimestamp(), true, t)))
                .filter(t -> t.getTime() <= upToIncluding);

        if (!applyStarts) {
            actions = actions.filter(t -> !(t.getTime() == upToIncluding && !t.isEnd()));
        }

        PriorityQueue<TimeElement<TemporalPlanAction>> applyQueue = new PriorityQueue<>(
                actions.collect(Collectors.toList()));

        double simulationTime = 0;
        TimeElement<TemporalPlanAction> head;
        while ((head = applyQueue.poll()) != null) {
            TemporalPlanAction action = head.getPayload();
            simulationTime = head.getTime();
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
        temporalPlanActions.stream().flatMapToDouble(t -> DoubleStream.of(t.getStartTimestamp(), t.getEndTimestamp()))
                .filter(t -> t > pointer.getTime()).min().ifPresent(res -> goToTime(res, false));
    }

    @Override
    public void goToPreviousCheckpoint() {
        temporalPlanActions.stream().flatMapToDouble(t -> DoubleStream.of(t.getStartTimestamp(), t.getEndTimestamp()))
                .filter(t -> t < pointer.getTime()).max()
                .ifPresent(res -> goToTime(res, false));
    }

    @Override
    public Optional<TemporalPlanAction> getLastAction() {
        return javaslang.collection.Stream.ofAll(temporalPlanActions).reverse()
                .dropWhile(t -> t.getStartTimestamp() > pointer.getTime())
                .dropWhile(t -> !pointer.isStartsApplied() && t.getStartTimestamp() == pointer.getTime())
                .take(1).toJavaOptional();
    }

    /**
     * Simple data struct, used in a time-oriented priority queue.
     * <p>
     * The ordering is based on the time. If the time is inconclusive, elements with {@code isEnd == true}
     * are ranked before {@code isEnd == false} (ends of actions before starts of actions).
     *
     * @param <Payload> the type of the payload object
     */
    private static class TimeElement<Payload> implements Comparable<TimeElement<Payload>> {

        private final double time;
        private final boolean isEnd;
        private final Payload payload;

        /**
         * Default constructor.
         *
         * @param time the time of the element (priority in the priority queue)
         * @param isEnd true iff this element represent an end of an action (not a start)
         * @param payload the payload object
         */
        TimeElement(double time, boolean isEnd, Payload payload) {
            this.payload = payload;
            this.isEnd = isEnd;
            this.time = time;
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
         * True iff this element represent an end of an action (not a start).
         *
         * @return true iff this element is an action end
         */
        public boolean isEnd() {
            return isEnd;
        }

        /**
         * Get the time (priority in the priority queue).
         *
         * @return the time
         */
        public double getTime() {
            return time;
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

            return new EqualsBuilder().append(time, that.time)
                    .append(isEnd, that.isEnd)
                    .append(payload, that.payload).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(time).append(isEnd)
                    .append(payload).toHashCode();
        }

        /**
         * The ordering is based on the time. If the time is inconclusive, elements with {@code isEnd == true}
         * are ranked before {@code isEnd == false} (ends of actions before starts of actions).
         * <p>
         * {@inheritDoc}
         */
        @Override
        public int compareTo(TimeElement<Payload> o) {
            return new CompareToBuilder().append(time, o.time)
                    .append(o.isEnd, isEnd) // reversed on purpose
                    .toComparison();
        }
    }
}
