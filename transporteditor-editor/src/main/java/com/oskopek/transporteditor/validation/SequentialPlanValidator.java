package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.state.PlanState;
import com.oskopek.transporteditor.model.state.SequentialPlanState;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.List;

/**
 * Validates based on the DOM we built when parsing.
 */
public class SequentialPlanValidator extends AbstractLogStreamable implements Validator {

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        if (!SequentialPlan.class.isInstance(plan)) {
            throw new IllegalArgumentException("Cannot validate non-sequential plan with sequential validator.");
        }
        return isValid(domain, (DefaultProblem) problem, (SequentialPlan) plan);
    }

    /**
     * Runs the validation and reports the results.
     *
     * @param domain the domain to validate against
     * @param problem the problem to validate against
     * @param plan the sequential plan to validate
     * @return true iff the plan is valid in the domain according to this validator
     */
    public boolean isValid(Domain domain, DefaultProblem problem, SequentialPlan plan) {
        List<Action> actionList = plan.getActions();
        PlanState state = new SequentialPlanState(domain, problem);
        log("Starting validation. Actions: " + actionList.size());
        for (Action action : actionList) {
            log("Validating: " + action);
            if (!action.arePreconditionsValid(state)) {
                log("Preconditions of \"" + action + "\"are invalid.");
                return false;
            }
            state.apply(action);
            if (!action.areEffectsValid(state)) {
                log("Effects of \"" + action + "\"are invalid.");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void log(String message) {
        super.log(new Date().toString() + ": " + message + "\n");
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 31).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof SequentialPlanValidator;
    }
}
