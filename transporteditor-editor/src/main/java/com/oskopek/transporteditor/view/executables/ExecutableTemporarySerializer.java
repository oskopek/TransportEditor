package com.oskopek.transporteditor.view.executables;

import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.persistence.*;
import javaslang.Tuple;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExecutableTemporarySerializer implements AutoCloseable {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final Path domainTmpFile;
    private Path problemTmpFile;
    private Path planTmpFile;

    public ExecutableTemporarySerializer(VariableDomain domain,
            DefaultProblem problem, Plan plan) throws IOException {
        VariableDomainIO domainIO = new VariableDomainIO();
        DefaultProblemIO problemIO = null;
        DataWriter<Plan> planIO = null;

        if (domain != null) {
            problemIO = new DefaultProblemIO(domain);
            if (problem != null) {
                if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
                    planIO = new TemporalPlanIO(domain, problem);
                } else {
                    planIO = new SequentialPlanIO(domain, problem);
                }
            }
        }

        String serializedDomain = domainIO.serialize(domain);
        String serializedProblem = problemIO == null ? "" : problemIO.serialize(problem);
        String serializedPlan = planIO == null ? "" : planIO.serialize(plan);
        domainTmpFile = Files.createTempFile("domain-", ".pddl");
        problemTmpFile = Files.createTempFile("problem-", ".pddl");
        planTmpFile = Files.createTempFile("plan-", ".val");

        try {
            Stream.of(Tuple.of(serializedDomain, domainTmpFile), Tuple.of(serializedProblem, problemTmpFile),
                    Tuple.of(serializedPlan, planTmpFile)).forEach(tpl -> {
                Path path = tpl._2;
                try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
                    writer.write(tpl._1);
                } catch (IOException e) {
                    logger.warn("An error occurred during creating and writing temp file: {}", path);
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e2) {
                        // intentionally ignore
                    }
                    throw new IllegalStateException("An error occurred during creating and writing temp files.", e);
                }
            });
        } catch (IllegalStateException e) {
            throw new IOException("An error occurred during creating and writing temp files.", e.getCause());
        }
    }

    public Path getDomainTmpFile() {
        return domainTmpFile;
    }

    public Path getProblemTmpFile() {
        return problemTmpFile;
    }

    public Path getPlanTmpFile() {
        return planTmpFile;
    }

    @Override
    public void close() throws IOException {
        if (!Files.deleteIfExists(planTmpFile)) {
            logger.warn("Couldn't delete plan temp file: {}", planTmpFile);
        }
        if (!Files.deleteIfExists(problemTmpFile)) {
            logger.warn("Couldn't delete problem temp file: {}", problemTmpFile);
        }
        if (!Files.deleteIfExists(domainTmpFile)) {
            logger.warn("Couldn't delete domain temp file: {}", domainTmpFile);
        }
    }
}
