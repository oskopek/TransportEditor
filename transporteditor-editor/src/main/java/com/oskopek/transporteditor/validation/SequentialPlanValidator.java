package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.state.PlanState;
import com.oskopek.transporteditor.model.state.SequentialPlanState;

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
        return isValid(domain, (DefaultProblem) problem, (SequentialPlan) plan);
    }

    public boolean isValid(Domain domain, DefaultProblem problem, SequentialPlan plan) {
        List<Action> actionList = plan.getActions();
        PlanState state = new SequentialPlanState(domain, problem);

        for (Action action : actionList) {
            if (!action.arePreconditionsValid(state)) {
                return false;
            }
            state.apply(action);
            if (!action.areEffectsValid(state)) {
                return false;
            }
        }
        return true;
    }
}
