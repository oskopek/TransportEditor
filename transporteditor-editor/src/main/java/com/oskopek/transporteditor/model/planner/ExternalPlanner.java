package com.oskopek.transporteditor.model.planner;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.TemporalPlanIO;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.ExecutableTemporarySerializer;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javaslang.control.Try;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ExternalPlanner extends AbstractLogStreamable implements Planner {

    private final ExecutableWithParameters executable;
    private transient ObjectProperty<Process> plannerProcessProperty = new SimpleObjectProperty<>();
    private transient ObjectProperty<Plan> bestPlan = new SimpleObjectProperty<>();

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
        String parameters = executable.getExecutableCommand();
        if (!parameters.contains("{0}") || !parameters.contains("{1}")) {
            throw new IllegalArgumentException("Executable command does not contain {0} and {1}.");
        }
        this.executable = executable;
    }

    protected Object readResolve() {
        super.readResolve();
        bestPlan = new SimpleObjectProperty<>();
        plannerProcessProperty = new SimpleObjectProperty<>();
        return this;
    }

    private Plan startPlanning(VariableDomain domain, DefaultProblem problem) {
        try (ExecutableTemporarySerializer serializer = new ExecutableTemporarySerializer(domain, problem, null)) {
            String filledIn = executable.getExecutableCommand(serializer.getDomainTmpFile().toAbsolutePath(),
                    serializer.getProblemTmpFile().toAbsolutePath());
            ProcessBuilder builder = new ProcessBuilder(filledIn);
            try {
                plannerProcessProperty.set(builder.start());
            } catch (IOException e) {
                throw new IllegalStateException("An error occurred during creating the model process.", e);
            }
            CompletableFuture<Integer> retValFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return plannerProcessProperty.get().waitFor();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Planning failed.", e);
                }
            }).toCompletableFuture();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader((plannerProcessProperty.get().getErrorStream())))) {
                String line = reader.readLine();
                while (line != null && !retValFuture.isDone()) {
                    sendToListeners(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading stderr of planner process.", e);
            }

            int retVal = Try.of(retValFuture::get)
                    .getOrElseThrow((e) -> new IllegalStateException("Failed waiting for planner process.", e));
            if (retVal != 0) {
                throw new IllegalStateException("Planning failed: return value " + retVal + ".");
            }

            StringBuilder planOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader((plannerProcessProperty.get().getInputStream())))) {
                String line = reader.readLine();
                while (line != null) {
                    sendToListeners(line);
                    planOutput.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading stdout of planner process.", e);
            }

            this.bestPlan.setValue(tryParsePlan(domain, problem, planOutput.toString()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist domain or problem, cannot plan.", e);
        }
        return getBestPlan();
    }

    private Plan tryParsePlan(VariableDomain domain, DefaultProblem problem, String planContents) {
        if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
            return new TemporalPlanIO(domain, problem).parse(planContents);
        } else {
            return new SequentialPlanIO(domain, problem).parse(planContents);
        }
    }

    @Override
    public CompletionStage<Plan> startAsync(Domain domain, Problem problem) {
        return CompletableFuture.supplyAsync(() -> startAndWait(domain, problem));
    }

    @Override
    public Plan startAndWait(Domain domain, Problem problem) {
        if (isPlanning().getValue()) {
            throw new IllegalStateException("Already planning!");
        }
        return startPlanning((VariableDomain) domain, (DefaultProblem) problem); // TODO: Fix casting properly
    }

    @Override
    public Plan getBestPlan() {
        return bestPlan.get();
    }

    @Override
    public ObservableValue<Plan> getCurrentPlan() {
        return bestPlan;
    }

    @Override
    public ObservableValue<Boolean> isPlanning() {
        return plannerProcessProperty.isNotNull();
    }

    private void sendToListeners(String logMessage) {
        forEach(l -> l.accept(logMessage));
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
