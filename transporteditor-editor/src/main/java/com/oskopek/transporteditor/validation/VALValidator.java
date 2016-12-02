package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A thin wrapper around the <a href="https://github.com/KCL-Planning/VAL">VAL validator</a> for any Transport domain
 * variant representable in PDDL.
 * <p>
 * First exports the domain to a PDDL file, then runs VAL on that and the exported plan.
 */
public class VALValidator extends AbstractLogStreamable implements Validator {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutableWithParameters executable;

    public VALValidator(String executable, String parameters) {
        this.executable = new DefaultExecutableWithParameters(executable, parameters);
        if (!parameters.contains("{0}") || !parameters.contains("{1}")) {
            throw new IllegalArgumentException("Executable command does not contain {0} and {1}.");
        }
    }

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public CompletionStage<Boolean> isValidAsync(Domain domain, Problem problem, Plan plan) {
        return CompletableFuture.supplyAsync(() -> isValid(domain, problem, plan));
    }
}
