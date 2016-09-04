package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;

/**
 * Represents a process that validates a plan against a given domain. See the {@link VALValidator} for an example.
 */
public interface Validator {

    /**
     * Runs the validation and reports the results.
     *
     * @param domain the domain to validate against
     * @param problem the problem to validate against
     * @param plan the plan to validate
     * @return true iff the plan is valid in the domain according to this validator
     */
    boolean isValid(Domain domain, Problem problem, Plan plan);

}
