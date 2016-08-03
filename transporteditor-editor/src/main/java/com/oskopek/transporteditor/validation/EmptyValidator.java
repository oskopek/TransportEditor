package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;

/**
 * Dummy validator: assumes all plans are correct.
 */
public class EmptyValidator implements Validator {

    @Override
    public boolean isValid(Plan plan, Domain domain) {
        return true;
    }
}
