package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.CancellableLogStreamable;
import com.oskopek.transporteditor.view.executables.Cancellable;
import com.oskopek.transporteditor.view.executables.ExecutableTemporarySerializer;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A thin wrapper around the <a href="https://github.com/KCL-Planning/VAL">VAL validator</a> for any Transport domain
 * variant representable in PDDL, or any external validator.
 * <p>
 * First exports the domain and problem to a PDDL file, then runs VAL on that and the exported plan. Uses a Process.
 * Logs the process' stderr and stdout via
 * {@link com.oskopek.transporteditor.view.executables.AbstractLogStreamable#log(String)}.
 * Returns true iff the process exits with a 0 return code. Is cancellable via {@link Cancellable#cancel()}.
 */
public class ValValidator extends CancellableLogStreamable implements Validator {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutableWithParameters executable;
    private final transient ObjectProperty<Process> validatorProcessProperty = new SimpleObjectProperty<>();

    /**
     * Construct a ValValidator from a given executable and its parameters.
     * <p>
     * {0}, {1} and {2} can be in any order. {0} is the domain filename, {1} is the path filename,
     * {3} is the plan filename.
     *
     * @param executable should contain templates {0}, {1} and {2} (for substitution)
     */
    public ValValidator(ExecutableWithParameters executable) {
        String parameters = executable.getParameters();
        if (!parameters.contains("{0}") || !parameters.contains("{1}") || !parameters.contains("{2}")) {
            throw new IllegalArgumentException("Executable command does not contain templates {0}, {1} and {2}.");
        }
        this.executable = executable;
    }

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        return isValidInternal(domain, problem, plan);
    }

    @Override
    public ExecutableWithParameters getExecutableWithParameters() {
        return executable;
    }

    /**
     * Runs the validation in a process and reports the results.
     *
     * @param domain the domain to validate against
     * @param problem the problem to validate against
     * @param plan the sequential plan to validate
     * @return true iff the plan is valid in the domain according to this validator
     */
    private synchronized boolean isValidInternal(Domain domain, Problem problem, Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Cannot validate null plan.");
        }
        try (ExecutableTemporarySerializer serializer = new ExecutableTemporarySerializer(domain, problem, plan)) {
            String executableCommand = executable.getExecutable();
            List<String> parameters = executable.getCommandParameterList(serializer.getDomainTmpFile().toAbsolutePath(),
                    serializer.getProblemTmpFile().toAbsolutePath(), serializer.getPlanTmpFile().toAbsolutePath());
            ProcessBuilder builder = new ProcessBuilder(parameters).redirectErrorStream(true);
            try {
                validatorProcessProperty.set(builder.start());
            } catch (IOException e) {
                throw new IllegalStateException("An error occurred during creating the validator process.", e);
            }
            CompletableFuture<Integer> retValFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    while (!shouldCancel()) {
                        boolean finished = validatorProcessProperty.get().waitFor(500, TimeUnit.MILLISECONDS);
                        if (finished) {
                            break;
                        }
                    }
                    if (shouldCancel()) {
                        validatorProcessProperty.get().destroyForcibly().waitFor();
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Validation failed.", e);
                }
                return validatorProcessProperty.get().exitValue();
            }).toCompletableFuture();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader((validatorProcessProperty.get().getInputStream())))) {
                String line = reader.readLine();
                while (line != null && !retValFuture.isDone()) {
                    logger.debug("stdout/err: {}", line);
                    log(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading stdout of validation process.", e);
            }

            int retVal = Try.of(retValFuture::get)
                    .getOrElseThrow((e) -> new IllegalStateException("Failed waiting for validation process.", e));
            if (retVal != 0) {
                logger.debug("Validation failed: return value {}.", retVal);
            }
            validatorProcessProperty.setValue(null);
            setShouldCancel(false);
            return retVal == 0;
        } catch (IOException e) {
            validatorProcessProperty.setValue(null);
            setShouldCancel(false);
            throw new IllegalStateException("Failed to persist domain, problem or plan - cannot validate.", e);
        }
    }

}
