package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thin wrapper around the <a href="https://github.com/KCL-Planning/VAL">VAL validator</a> for any Transport domain
 * variant representable in PDDL.
 * <p>
 * First exports the domain to a PDDL file, then runs VAL on that and the exported plan.
 */
public class VALValidator implements Validator {

    private final String executableString;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public VALValidator(String executableString) {
        if (!executableString.contains("{0}") || !executableString.contains("{1}")) {
            throw new IllegalArgumentException("Executable string does not contain {0} and {1}.");
        }
        this.executableString = executableString;
    }

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
