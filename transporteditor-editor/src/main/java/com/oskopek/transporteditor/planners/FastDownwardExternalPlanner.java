package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;

/**
 * Wrapper of the wrapper script ({@code fast-down-plan.sh})
 * for the <a href="http://www.fast-downward.org/">Fast Downward</a> planner.
 */
public class FastDownwardExternalPlanner extends ExternalPlanner {

    private static final String executable = "fast-down-plan.sh";
    private static final String defaultSearchParameters = "--search astar(lmcut())";

    /**
     * Default empty constructor.
     */
    public FastDownwardExternalPlanner() {
        this("", defaultSearchParameters);
    }

    public FastDownwardExternalPlanner(String parameters) {
        super(executable, parameters);
    }

    public FastDownwardExternalPlanner(String beforeArgs, String afterArgs) {
        super(executable, beforeArgs + " {0} {1} " + afterArgs);
    }

    protected FastDownwardExternalPlanner(ExecutableWithParameters executableWithParameters) {
        super(executableWithParameters);
    }

    /**
     * Only available on systems with a shell ({@code sh}), <a href="http://www.fast-downward.org/">Fast Downward</a>
     * and a wrapper script ({@code fast-down-plan.sh}).
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return new DefaultExecutableWithParameters(executable, "").isExecutableValid()
                && new DefaultExecutableWithParameters("sh", "").isExecutableValid();
    }

    @Override
    public FastDownwardExternalPlanner copy() {
        return new FastDownwardExternalPlanner(getExecutableWithParameters());
    }
}
