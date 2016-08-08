package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.Problem;

/**
 * Dummy validator: assumes all plans are correct.
 */
public class EmptyValidator implements Validator {

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        return true;
    }
}
