/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.domain.action.Action;
import com.oskopek.transporteditor.planning.domain.action.predicates.TemporalQuantifier;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.plan.PlanEntry;
import com.oskopek.transporteditor.planning.plan.visualization.DefaultVisualizer;
import com.oskopek.transporteditor.planning.plan.visualization.PlanState;
import com.oskopek.transporteditor.planning.plan.visualization.TimePoint;
import com.oskopek.transporteditor.planning.problem.Problem;

/**
 * Validates based on the DOM we built when parsing. Uses visualization objects.
 */
public class DefaultValidator implements Validator {

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        DefaultVisualizer visualizer = new DefaultVisualizer();
        PlanEntry first = plan.firstEntries().get(0);
        PlanState state = visualizer.build(domain, problem, plan,
                new TimePoint(first.getStartTimestamp(), TemporalQuantifier.AT_START, first));
        return validateAll(state);
    }

    private boolean validateTimePoint(PlanState state, TimePoint timePoint) {
        Action action = timePoint.getPlanEntry().getAction();
        action.arePreconditionsValid(state);
    }

    private boolean validateAll(PlanState state) {
        return validateTimePoint(state, state.getTimePoint()) && state.childTimePoints().stream().map(
                state::applyDiffToState).map(this::validateAll).reduce(true, Boolean::logicalAnd);
    }
}
