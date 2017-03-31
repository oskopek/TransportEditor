package com.oskopek.transport.benchmark.config;

import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.benchmark.ScoreFunction;

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
                return (domain, problem, plan) -> (double) plan.getTemporalPlanActions().size();
            case TOTAL_TIME:
                return (domain, problem, plan) -> plan.getTemporalPlanActions().stream().mapToDouble(
                        TemporalPlanAction::getEndTimestamp).max().orElse(0d);
            default:
                throw new IllegalStateException("Unknown score function type.");
        }
    }

}
