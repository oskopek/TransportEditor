package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;

/**
 * Wrapper of the wrapper script ({@code fast-down-plan.sh})
 * for the <a href="http://www.fast-downward.org/">Fast Downward</a> planner.
 */
public class FastDownwardExternalPlanner extends ExternalPlanner {

    private static final String executable = "fast-down-plan.sh";
    private static final String defaultParamaters = "--search \"astar(lmcut())\"";

    /**
     * Default empty constructor.
     */
    public FastDownwardExternalPlanner() {
        this(defaultParamaters);
    }

    /**
     * Constructor with fast-downward command line arguments.
     *
     * @param defaultParamaters the command line args
     */
    public FastDownwardExternalPlanner(String defaultParamaters) {
        super(executable, "{0} {1} " + defaultParamaters);
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
}
