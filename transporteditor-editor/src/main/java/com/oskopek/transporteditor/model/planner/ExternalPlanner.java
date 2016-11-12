package com.oskopek.transporteditor.model.planner;

import com.google.common.io.Files;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.plan.TemporalPlan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.DefaultProblemIO;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.TemporalPlanIO;
import com.oskopek.transporteditor.persistence.VariableDomainIO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;

public class ExternalPlanner implements Planner {

    private final String executableString;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private VariableDomain domain;
    private DefaultProblem problem;
    private File domainTmp;
    private File problemTmp;
    private Process plannerProcess;
    private Plan bestPlan;

    /**
     * {0} and {1} can be in any order. {0} is the domain filename, {1} is the path filename.
     *
     * @param executableString in the format: "/path/to/executable ... {0} ... {1}"
     */
    public ExternalPlanner(String executableString) {
        if (!executableString.contains("{0}") || !executableString.contains("{1}")) {
            throw new IllegalArgumentException("Executable string does not contain {0} and {1}.");
        }
        this.executableString = executableString;
    }

    private boolean isPlanning() {
        return plannerProcess != null;
    }

    @Override
    public void startPlanning(Domain domain, Problem problem) {
        if (isPlanning()) {
            throw new IllegalStateException("Already model!");
        }
        VariableDomainIO io = new VariableDomainIO();
        DefaultProblemIO problemIO = new DefaultProblemIO(domain);
        startPlanning((VariableDomain) domain, io, (DefaultProblem) problem, problemIO); // TODO: Fix me properly
    }

    private void startPlanning(VariableDomain domain, VariableDomainIO io, DefaultProblem problem,
            DefaultProblemIO problemIO) {
        String serializedDomain = io.serialize(domain);
        String serializedProblem = problemIO.serialize(problem);

        try {
            domainTmp = File.createTempFile("domain-", ".pddl");
            problemTmp = File.createTempFile("problem-", ".pddl");

            try (BufferedWriter writer = Files.newWriter(domainTmp, Charset.forName("UTF-8"))) {
                writer.write(serializedDomain);
            }
            try (BufferedWriter writer = Files.newWriter(problemTmp, Charset.forName("UTF-8"))) {
                writer.write(serializedProblem);
            }
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred during creating and writing temp files.", e);
        }

        if (domainTmp == null || problemTmp == null) {
            throw new IllegalStateException("Failed to persist domain and problem, cannot plan.");
        }

        String filledIn = MessageFormat.format(executableString, domainTmp.getAbsolutePath(),
                problemTmp.getAbsolutePath());

        ProcessBuilder builder = new ProcessBuilder(filledIn);
        builder.redirectErrorStream(true);
        try {
            plannerProcess = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred during creating the model process.", e);
        }
    }

    @Override
    public void stopPlanning() {
        int retVal;
        try {
            retVal = plannerProcess.waitFor();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Planning failed.", e);
        }

        if (!problemTmp.delete()) {
            logger.warn("Couldn't delete problem temp file: {}", problemTmp.getAbsoluteFile());
        }
        if (!domainTmp.delete()) {
            logger.warn("Couldn't delete domain temp file: {}", domainTmp.getAbsoluteFile());
        }
        problemTmp = null;
        domainTmp = null;

        logger.debug("Planner output:");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((plannerProcess.getErrorStream())))) {
            while (reader.ready()) {
                logger.debug("PLANNER: stderr: {}", reader.readLine());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading stderr of planner process.", e);
        }

        if (retVal != 0) {
            throw new IllegalStateException("Planning failed: return value " + retVal + ".");
        }

        StringBuilder planOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((plannerProcess.getInputStream())))) {
            while (reader.ready()) {
                String line = reader.readLine();
                logger.debug("PLANNER: stdout: {}", line);
                planOutput.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading stderr of planner process.", e);
        }

        this.bestPlan = tryParsePlan(planOutput.toString());

        this.domainTmp = null;
        this.problemTmp = null;
        this.problem = null;
        this.domain = null;
        this.plannerProcess = null;
    }

    private TemporalPlan tryParseTemporalPlan(String planContents) {
        return new TemporalPlanIO(domain, problem).parse(planContents);
    }

    private SequentialPlan tryParseSequentialPlan(String planContents) {
        return new SequentialPlanIO(domain, problem).parse(planContents);
    }

    private Plan tryParsePlan(String planContents) {
        if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
            return tryParseTemporalPlan(planContents);
        } else {
            return tryParseSequentialPlan(planContents);
        }
    }

    @Override
    public Plan getBestPlan() {
        return bestPlan;
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
        return new EqualsBuilder().append(executableString, that.executableString).append(domain, that.domain).append(
                problem, that.problem).append(getBestPlan(), that.getBestPlan()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(executableString).append(domain).append(problem).append(getBestPlan())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("executableString", executableString).toString();
    }
}
