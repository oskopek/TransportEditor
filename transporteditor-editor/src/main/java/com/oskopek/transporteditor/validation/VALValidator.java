package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;

/**
 * A thin wrapper around the <a href="https://github.com/KCL-Planning/VAL">VAL validator</a> for any Transport domain
 * variant representable in PDDL.
 *
 * First exports the domain to a PDDL file, then runs VAL on that and the exported plan.
 */
public class VALValidator implements Validator {

    @Override
    public boolean isValid(Plan plan, Domain domain) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
