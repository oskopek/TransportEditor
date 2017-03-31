package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.planner.ExternalPlanner;
import com.oskopek.transport.tools.executables.DefaultExecutableWithParameters;
import com.oskopek.transport.tools.executables.ExecutableWithParameters;

/**
 * Wrapper of the wrapper script ({@code tfd-plan.sh})
 * for the <a href="http://gki.informatik.uni-freiburg.de/tools/tfd/">Temporal Fast Downward</a> planner.
 */
public class TFDExternalPlanner extends ExternalPlanner {

    private static final String executable = "tfd-plan.sh";
    private static final String defaultSearchParameters = "{0} {1} {2}";

    /**
     * Default empty constructor.
     */
    public TFDExternalPlanner() {
        this(defaultSearchParameters);
    }

    /**
     * Constructor for the {@code tfd-plan.sh} script with any parameters.
     * <p>
     * Parameter templates: {0}, {1} and {2} can be in any order. {0} is the domain filename, {1} is the path filename,
     * and {2} is the output file.
     *
     * @param parameters in the format: "... {0} ... {1} ... {2}"
     */
    public TFDExternalPlanner(String parameters) {
        super(executable, parameters);
    }

    /**
     * Overridden constructor, used in {@link #copy()}.
     *
     * @param executableWithParameters the executable with parameters
     * @param name the name
     */
    public TFDExternalPlanner(ExecutableWithParameters executableWithParameters, String name) {
        super(executableWithParameters, name);
    }

    /**
     * Only available on systems with a shell ({@code sh}), <a href="http://www.fast-downward.org/">Fast Downward</a>
     * and a wrapper script ({@code tfd-plan.sh}).
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return new DefaultExecutableWithParameters(executable, "").isExecutableValid()
                && new DefaultExecutableWithParameters("sh", "").isExecutableValid();
    }

    @Override
    public TFDExternalPlanner copy() {
        return new TFDExternalPlanner(getExecutableWithParameters(), getName());
    }
}
