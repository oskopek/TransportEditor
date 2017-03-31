package com.oskopek.transport.model.planner;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.persistence.TemporalPlanIO;
import com.oskopek.transport.tools.executables.CancellableLogStreamable;
import com.oskopek.transport.tools.executables.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javaslang.control.Try;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A thin wrapper around an external executable planner for any Transport domain
 * variant representable in PDDL.
 * <p>
 * First exports the domain and problem to a PDDL file, then runs the executable on that and parses the exported plan
 * from a specified path. Uses a {@link Process}. Logs the process' stderr and stdout via
 * {@link com.oskopek.transport.tools.executables.AbstractLogStreamable#log(String)}.
 * Returns a success iff the process exits with a 0 return code and the plan is parsable from the output file.
 * Is cancellable via {@link Cancellable#cancel()}. Does not have a no-arg constructor, because it is a
 * special case, handled separately in the UI.
 */
public class ExternalPlanner extends CancellableLogStreamable implements Planner {

    private transient String name;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutableWithParameters executable;
    private final transient ObjectProperty<Process> plannerProcessProperty = new SimpleObjectProperty<>();
    private final transient ObjectProperty<Plan> bestPlan = new SimpleObjectProperty<>();

    /**
     * Assumes stdout as plan, stderr for status updates.
     * <p>
     * Parameter templates: {0} and {1} and {2} can be in any order. {0} is the domain filename,
     * {1} is the path filename and {2} is the output filename.
     *
     * @param executableString an executable in the system path or an executable file
     * @param parameters in the format: "... {0} ... {1} ... {2} .."
     */
    public ExternalPlanner(String executableString, String parameters) {
        this(new DefaultExecutableWithParameters(executableString, parameters));
    }

    /**
     * Assumes stdout as plan, stderr for status updates.
     * <p>
     * Parameter templates: {0}, {1} and {2} can be in any order. {0} is the domain filename, {1} is the path filename,
     * and {2} is the output filename.
     *
     * @param executableWithParametersString an executable in the system path or an executable file and parameters
     * in the format: "... {0} ... {1} ... {2} ..." in a single string
     */
    public ExternalPlanner(String executableWithParametersString) {
        this(executableWithParametersString.split(" ")[0],
                executableWithParametersString.substring(executableWithParametersString.indexOf(' ') + 1));
    }

    /**
     * Assumes stdout as plan, stderr for status updates.
     * <p>
     * Parameter templates: {0}, {1} and {2} can be in any order. {0} is the domain filename, {1} is the path filename
     * and {2} is the output filename.
     *
     * @param executable an executable with correct parameter templates
     */
    public ExternalPlanner(ExecutableWithParameters executable) {
        this(executable, ExternalPlanner.class.getSimpleName() + '[' + executable.getExecutable() + ' '
                + executable.getParameters() + ']');
    }

    /**
     * Assumes stdout as plan, stderr for status updates.
     * <p>
     * Parameter templates: {0}, {1} and {2} can be in any order. {0} is the domain filename, {1} is the path filename,
     * and {2} is the output filename.
     *
     * @param executable an executable with correct parameter templates
     * @param name the name of this planner
     */
    public ExternalPlanner(ExecutableWithParameters executable, String name) {
        String parameters = executable.getParameters();
        if (!parameters.contains("{0}") || !parameters.contains("{1}") || !parameters.contains("{2}")) {
            logger.warn("Executable command does not contain {0}, {1} and {2} parameter templates.");
        }
        this.executable = executable;
        this.name = name;
    }

    /**
     * Deserialization helper method.
     *
     * @return a new immutable external planner with correctly initialized transient fields
     */
    protected Object readResolve() {
        return new ExternalPlanner(executable);
    }

    /**
     * Run the external planning process and supply the cancellation option.
     *
     * @param domain the domain to plan with
     * @param problem the problem to plan
     * @return the computed plan, or null in case of an error
     * @throws IllegalStateException if an error occurred during planning
     */
    private synchronized Plan plan(Domain domain, Problem problem) {
        try (ExecutableTemporarySerializer serializer = new ExecutableTemporarySerializer(domain, problem, null)) {
            List<String> parameters = executable.getCommandParameterList(serializer.getDomainTmpFile().toAbsolutePath(),
                    serializer.getProblemTmpFile().toAbsolutePath(), serializer.getPlanTmpFile().toAbsolutePath());
            ProcessBuilder builder = new ProcessBuilder(parameters);
            builder.redirectErrorStream(true);
            try {
                plannerProcessProperty.set(builder.start());
            } catch (IOException e) {
                throw new IllegalStateException("An error occurred during creating the planner process.", e);
            }
            CompletableFuture<Integer> retValFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    while (!shouldCancel()) {
                        boolean finished = plannerProcessProperty.get().waitFor(500, TimeUnit.MILLISECONDS);
                        if (finished) {
                            break;
                        }
                    }
                    if (shouldCancel()) {
                        logger.debug("Destroying process...");
                        plannerProcessProperty.get().destroy();
                        logger.debug("Destroyed process.");
                        return 1;
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Planning failed.", e);
                }
                return plannerProcessProperty.get().exitValue();
            }).toCompletableFuture();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(plannerProcessProperty.get()
                    .getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug(line);
                    log(line);
                }
            } catch (IOException e) {
                if (e.getMessage().contains("Stream closed")) {
                    logger.warn("Stream closed while reading stdout.");

                } else {
                    throw new IllegalStateException("Error while reading stderr/out of planner process.", e);
                }
            }

            int retVal = Try.of(retValFuture::get)
                    .getOrElseThrow(e -> new IllegalStateException("Failed waiting for planner process.", e));
            if (retVal != 0) {
                logger.warn("Planning failed: return value {}.", retVal);
                bestPlan.setValue(null);
            }

            // Try to parse a plan even if planning failed:
            Try.run(() -> Thread.sleep(1500)); // sleep to let the filesystem rest
            StringBuilder planOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(serializer.getPlanTmpFile())))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("plan: {}", line);
                    planOutput.append(line).append('\n');
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading plan file.", e);
            }
            this.bestPlan.setValue(tryParsePlan(domain, problem, planOutput.toString()));
        } catch (IOException e) {
            setShouldCancel(false);
            plannerProcessProperty.setValue(null);
            throw new IllegalStateException("Failed to persist domain or problem, cannot plan.", e);
        } catch (Throwable e) {
            setShouldCancel(false);
            plannerProcessProperty.setValue(null);
            throw new IllegalStateException(e);
        }
        setShouldCancel(false);
        plannerProcessProperty.setValue(null);
        return getCurrentPlan();
    }

    /**
     * Tries to parse a plan based on its string representation. A util method to differentiate between temporal
     * and sequential plans and delegating. Used for parsing stdout of external planners.
     *
     * @param domain the domain that was planned with
     * @param problem the problem that was planned
     * @param planContents the contents to parse
     * @return the parsed plan, or null in case of an error
     * @see TemporalPlanIO
     * @see SequentialPlanIO
     */
    private static Plan tryParsePlan(Domain domain, Problem problem, String planContents) {
        if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
            return new TemporalPlanIO(domain, problem).parse(planContents);
        } else {
            return new SequentialPlanIO(domain, problem).parse(planContents);
        }
    }

    @Override
    public synchronized Plan startAndWait(Domain domain, Problem problem) {
        if (isPlanning().getValue()) {
            throw new IllegalStateException("Already planning!");
        }
        return plan(domain, problem);
    }

    @Override
    public ObservableValue<Plan> currentPlanProperty() {
        return bestPlan;
    }

    @Override
    public synchronized ObservableValue<Boolean> isPlanning() {
        return plannerProcessProperty.isNotNull();
    }

    @Override
    public ExecutableWithParameters getExecutableWithParameters() {
        return executable;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(executable)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalPlanner)) {
            return false;
        }
        ExternalPlanner that = (ExternalPlanner) o;
        return new EqualsBuilder()
                .append(executable, that.executable)
                .isEquals();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("executable", executable).toString();
    }

    @Override
    public ExternalPlanner copy() {
        return new ExternalPlanner(executable, name);
    }
}
