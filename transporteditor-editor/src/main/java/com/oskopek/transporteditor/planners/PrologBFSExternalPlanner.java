package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Wrapper for the Prolog-based BFS planner written in the Non-procedural programming class.
 * Only available on systems with a shell ({@code sh}) and <a href="http://www.swi-prolog.org/">SWIPL</a>.
 */
public final class PrologBFSExternalPlanner extends ExternalPlanner {

    private static final Path executable = Paths.get("..", "nprg", "plan.sh");

    /**
     * Default empty constructor.
     */
    public PrologBFSExternalPlanner() {
        super(executable.toAbsolutePath().toString(), "{1}");
    }

    /**
     * Only available on systems with a shell ({@code sh}) and <a href="http://www.swi-prolog.org/">SWIPL</a>.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return Files.isExecutable(executable) && new DefaultExecutableWithParameters("swipl", "").isExecutableValid()
                && new DefaultExecutableWithParameters("sh", "").isExecutableValid();
    }

    @Override
    public PrologBFSExternalPlanner copy() {
        PrologBFSExternalPlanner copy = new PrologBFSExternalPlanner();
        copy.setName(getName());
        return copy;
    }
}
