package com.oskopek.transporteditor.planners.benchmark.config;

import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;

/**
 * Used for deserializing common score functions.
 */
public enum ScoreFunctionType {

    /**
     * The number of actions in a plan.
     */
    ACTION_COUNT,

    /**
     * The end time of the last action.
     */
    TOTAL_TIME;

    /**
     * Create a {@link ScoreFunction} instance from the given score function type.
     *
     * @return the score function
     * @throws IllegalStateException if the given score function type is not recognized
     */
    public ScoreFunction toScoreFunction() {
        switch (this) {
            case ACTION_COUNT:
                return (domain, problem, plan) -> plan.getTemporalPlanActions().size();
            case TOTAL_TIME:
                return (domain, problem, plan) -> plan.getTemporalPlanActions().stream().mapToInt(
                        t -> t.getEndTimestamp()).max().orElse(0);
            default:
                throw new IllegalStateException("Unknown score function type.");
        }
    }

}
