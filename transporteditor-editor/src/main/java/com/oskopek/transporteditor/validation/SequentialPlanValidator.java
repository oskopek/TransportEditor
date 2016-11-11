/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Problem;

import java.util.List;

/**
 * Validates based on the DOM we built when parsing.
 */
public class SequentialPlanValidator implements Validator {

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        if (!SequentialPlan.class.isInstance(plan)) {
            throw new IllegalArgumentException("Cannot validate non-sequential plan with sequential validator.");
        }
        return isValid((DefaultProblem) problem, (SequentialPlan) plan);
    }

    public boolean isValid(DefaultProblem problem, SequentialPlan plan) {
        List<Action> actionList = plan.getActions();
        Problem instance = new DefaultProblem(problem);

        for (Action action : actionList) {
            if (!action.arePreconditionsValid(instance)) {
                return false;
            }
            instance = plan.apply(instance, action);
            if (!action.areEffectsValid(instance)) {
                return false;
            }
        }
        return true;
    }
}
