/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.TemporalPlan;
import com.oskopek.transporteditor.model.problem.Problem;

import java.util.Optional;
import java.util.Set;

public class TemporalPlanIO implements DataReader<TemporalPlan>, DataWriter<TemporalPlan> {

    private final Domain domain;
    private final Problem problem;

    public TemporalPlanIO(Domain domain, Problem problem) {
        this.domain = domain;
        this.problem = problem;
    }

    public static String serializeTemporalPlanAction(TemporalPlanAction temporalPlanAction) {
        String action = SequentialPlanIO.serializeAction(temporalPlanAction.getAction());
        StringBuilder str = new StringBuilder();
        str.append(temporalPlanAction.getStartTimestamp()).append(';').append(temporalPlanAction.getEndTimestamp())
                .append(" ").append(action).append('\n');
        return str.toString();
    }

    @Override
    public String serialize(TemporalPlan plan) throws IllegalArgumentException {
        StringBuilder str = new StringBuilder();
        Set<TemporalPlanAction> actionSet = plan.getTemporalPlanActions();
        actionSet.forEach(temporalPlanAction -> str.append(serializeTemporalPlanAction(temporalPlanAction)));

        Integer totalTime = 0;
        Optional<Integer> last = actionSet.stream().map(TemporalPlanAction::getEndTimestamp).max(Integer::compare);
        Optional<Integer> first = actionSet.stream().map(TemporalPlanAction::getStartTimestamp).min(Integer::compare);
        if (last.isPresent() && first.isPresent()) {
            totalTime = last.get() - first.get();
        }
        str.append("; total-time = ").append(totalTime).append(" (general-cost)\n");
        return str.toString();
    }

    @Override
    public TemporalPlan parse(String contents) throws IllegalArgumentException {
        return null;
    }


}
