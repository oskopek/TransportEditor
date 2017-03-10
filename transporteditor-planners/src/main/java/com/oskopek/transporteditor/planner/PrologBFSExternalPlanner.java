package com.oskopek.transporteditor.planner;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PrologBFSExternalPlanner extends ExternalPlanner {

    private static final Path executable = Paths.get("..", "nprg", "plan.sh");

    public PrologBFSExternalPlanner() {
        super(executable.toAbsolutePath().toString(), "{1}");
    }

    @Override
    public boolean isAvailable() {
        return Files.isExecutable(executable) && new DefaultExecutableWithParameters("swipl", "").isExecutableValid()
                && new DefaultExecutableWithParameters("sh", "").isExecutableValid();
    }
}
