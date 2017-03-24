package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;

/**
 * A thin wrapper around the <a href="https://github.com/KCL-Planning/VAL">VAL validator</a> for any Transport domain
 * variant representable in PDDL.
 * <p>
 * Extends an {@link ExternalValidator}.
 */
public class ValValidator extends ExternalValidator {

    private static final String executable = "validate";
    private static final String parameters = "{0} {1} {2}";

    /**
     * Default constructor.
     */
    public ValValidator() {
        super(new DefaultExecutableWithParameters(executable, parameters));
    }
}
