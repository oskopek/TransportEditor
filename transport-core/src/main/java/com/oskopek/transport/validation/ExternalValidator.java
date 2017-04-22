package com.oskopek.transport.validation;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.tools.executables.*;
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
 * A thin wrapper around an external executable validator for any Transport domain variant representable in PDDL.
 * <p>
 * First exports the domain and problem to a PDDL file, then runs VAL on that and the exported plan. Uses a Process.
 * Logs the process' stderr and stdout via
 * {@link com.oskopek.transport.tools.executables.AbstractLogStreamable#log(String)}.
 * Returns true iff the process exits with a 0 return code. Is cancellable via {@link Cancellable#cancel()}.
 */
public class ExternalValidator extends CancellableLogStreamable implements Validator {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutableWithParameters executable;
    private final transient ObjectProperty<Process> validatorProcessProperty = new SimpleObjectProperty<>();

    /**
     * Construct a ExternalValidator from a given executable and its parameters.
     * <p>
     * {0}, {1} and {2} can be in any order. {0} is the domain filename, {1} is the path filename,
     * {3} is the plan filename.
     *
     * @param executable should contain templates {0}, {1} and {2} (for substitution)
     */
    public ExternalValidator(ExecutableWithParameters executable) {
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
    public boolean isAvailable() {
        return executable != null && executable.isExecutableValid();
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
