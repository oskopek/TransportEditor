package com.oskopek.transporteditor.planners.benchmark.config;

import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;

public enum ScoreFunctionType {

    ACTION_COUNT, TOTAL_TIME;

    public ScoreFunction toScoreFunction() {
        switch (this) {
            case ACTION_COUNT:
                return (domain, problem, plan) -> plan.getTemporalPlanActions().size();
            case TOTAL_TIME:
                return (domain, problem, plan) -> plan.getTemporalPlanActions().stream()
                .mapToInt(t -> t.getEndTimestamp()).max().orElse(0);
            default:
                throw new IllegalStateException("Unknown score function type.");
        }
    }

}
