package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.domain.Domain;
import com.oskopek.transporteditor.plan.Plan;

/**
 * Represents a process that validates a plan against a given domain. See the {@link VALValidator} for an example.
 */
public interface Validator {

    /**
     * Runs the validation and reports the results.
     *
     * @param plan the plan to validate
     * @param domain the domain to validate against
     * @return true iff the plan is valid in the domain according to this validator
     */
    boolean isValid(Plan plan, Domain domain);

}
