package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;
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
import java.util.concurrent.CompletableFuture;

/**
 * A thin wrapper around the <a href="https://github.com/KCL-Planning/VAL">VAL validator</a> for any Transport domain
 * variant representable in PDDL, or any external validator.
 * <p>
 * First exports the domain to a PDDL file, then runs VAL on that and the exported plan.
 */
public class VALValidator extends AbstractLogStreamable implements Validator {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutableWithParameters executable;
    private transient ObjectProperty<Process> validatorProcessProperty = new SimpleObjectProperty<>();

    public VALValidator(ExecutableWithParameters executable) {
        String parameters = executable.getExecutableCommand();
        if (!parameters.contains("{0}") || !parameters.contains("{1}") || !parameters.contains("{2}")) {
            throw new IllegalArgumentException("Executable command does not contain templates {0}, {1} and {2}.");
        }
        this.executable = executable;
    }

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        return validate((VariableDomain) domain, (DefaultProblem) problem, plan); // TODO: casting fix
    }

    private synchronized boolean validate(VariableDomain domain, DefaultProblem problem, Plan plan) {
        try (ExecutableTemporarySerializer serializer = new ExecutableTemporarySerializer(domain, problem, plan)) {
            String filledIn = executable.getExecutableCommand(serializer.getDomainTmpFile().toAbsolutePath(),
                    serializer.getProblemTmpFile().toAbsolutePath());
            ProcessBuilder builder = new ProcessBuilder(filledIn).redirectErrorStream(true);
            try {
                validatorProcessProperty.set(builder.start());
            } catch (IOException e) {
                throw new IllegalStateException("An error occurred during creating the validator process.", e);
            }
            CompletableFuture<Integer> retValFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return validatorProcessProperty.get().waitFor();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Validation failed.", e);
                }
            }).toCompletableFuture();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader((validatorProcessProperty.get().getInputStream())))) {
                String line = reader.readLine();
                while (line != null && !retValFuture.isDone()) {
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
            return retVal != 0;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist domain, problem or plan - cannot validate.", e);
        }
    }
}
