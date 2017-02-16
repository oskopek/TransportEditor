package com.oskopek.transporteditor.model.planner;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.TemporalPlanIO;
import com.oskopek.transporteditor.view.executables.AbstractLogCancellable;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.ExecutableTemporarySerializer;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javaslang.control.Try;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ExternalPlanner extends AbstractLogCancellable implements Planner {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutableWithParameters executable;
    private final transient ObjectProperty<Process> plannerProcessProperty = new SimpleObjectProperty<>();
    private final transient ObjectProperty<Plan> bestPlan = new SimpleObjectProperty<>();

    /**
     * Assumes stdout as plan, stderr for status updates.
     * <p>
     * {0} and {1} can be in any order. {0} is the domain filename, {1} is the path filename.
     *
     * @param executableString an executable in the system path or an executable file
     * @param parameters in the format: "... {0} ... {1} ..."
     */
    public ExternalPlanner(String executableString, String parameters) {
        this(new DefaultExecutableWithParameters(executableString, parameters));
    }

    public ExternalPlanner(ExecutableWithParameters executable) {
        String parameters = executable.getParameters();
        if (!parameters.contains("{0}") || !parameters.contains("{1}")) {
            throw new IllegalArgumentException("Executable command does not contain {0} and {1}.");
        }
        this.executable = executable;
    }

    protected Object readResolve() {
        return new ExternalPlanner(getExecutableWithParameters());
    }

    private synchronized Plan startPlanning(Domain domain, Problem problem) {
        try (ExecutableTemporarySerializer serializer = new ExecutableTemporarySerializer(domain, problem, null)) {
            String executableCommand = executable.getExecutable();
            String filledIn = executable.getParameters(serializer.getDomainTmpFile().toAbsolutePath(),
                    serializer.getProblemTmpFile().toAbsolutePath());
            String[] splitFilledIn = filledIn.split(" ");
            List<String> parameters = new ArrayList<>(splitFilledIn.length + 1);
            parameters.add(executableCommand);
            parameters.addAll(Arrays.asList(splitFilledIn));
            ProcessBuilder builder = new ProcessBuilder(parameters);
            try {
                plannerProcessProperty.set(builder.start());
            } catch (IOException e) {
                throw new IllegalStateException("An error occurred during creating the planner process.", e);
            }
            CompletableFuture<Integer> retValFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    while (!getKillActiveProcess()) {
                        boolean finished = plannerProcessProperty.get().waitFor(500, TimeUnit.MILLISECONDS);
                        if (finished) {
                            break;
                        }
                    }
                    if (getKillActiveProcess()) {
                        plannerProcessProperty.get().destroyForcibly().waitFor();
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Planning failed.", e);
                }
                return plannerProcessProperty.get().exitValue();
            }).toCompletableFuture();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader((plannerProcessProperty.get().getErrorStream())))) {
                String line = reader.readLine();
                while (line != null && !retValFuture.isDone()) {
                    logger.debug("stderr: {}", line);
                    log("ERROR: " + line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading stderr of planner process.", e);
            }

            int retVal = Try.of(retValFuture::get)
                    .getOrElseThrow((e) -> new IllegalStateException("Failed waiting for planner process.", e));
            if (retVal != 0) {
                logger.warn("Planning failed: return value " + retVal + ".");
                bestPlan.setValue(null);
            } else {
                log("");
                log("Planner output:");
                StringBuilder planOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader((plannerProcessProperty.get().getInputStream())))) {
                    String line = reader.readLine();
                    while (line != null) {
                        logger.debug("stdout: {}", line);
                        log(line);
                        planOutput.append(line).append('\n');
                        line = reader.readLine();
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Error while reading stdout of planner process.", e);
                }
                this.bestPlan.setValue(tryParsePlan(domain, problem, planOutput.toString()));
            }
        } catch (IOException e) {
            setKillActiveProcess(false);
            plannerProcessProperty.setValue(null);
            throw new IllegalStateException("Failed to persist domain or problem, cannot plan.", e);
        }
        setKillActiveProcess(false);
        plannerProcessProperty.setValue(null);
        return getCurrentPlan();
    }

    private Plan tryParsePlan(Domain domain, Problem problem, String planContents) {
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
        return startPlanning(domain, problem);
    }

    @Override
    public Plan getCurrentPlan() {
        return bestPlan.get();
    }

    @Override
    public ObservableValue<Plan> currentPlanProperty() {
        return bestPlan;
    }

    @Override
    public synchronized ObservableValue<Boolean> isPlanning() {
        return plannerProcessProperty.isNotNull();
    }

    public ExecutableWithParameters getExecutableWithParameters() {
        return executable;
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
}
