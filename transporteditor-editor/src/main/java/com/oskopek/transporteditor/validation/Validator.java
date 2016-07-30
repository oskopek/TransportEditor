package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.domain.Domain;
import com.oskopek.transporteditor.plan.Plan;

public interface Validator {

    boolean isValid(Plan plan, Domain domain);

}
