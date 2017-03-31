package com.oskopek.transport.tools.executables;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.*;
import javaslang.Tuple;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Auto-closeable PDDL serializer of model objects. Makes them available at temporary locations and tries to delete
 * them at close. To be used with Java 8's try-with-resources.
 * <pre>
 * {@code
 * try (ExecutableTemporarySerializer serializer = new ExecutableTemporarySerializer(domain, problem, null)) {
 *     ...
 * }
 * }
 * </pre>
 */
public class ExecutableTemporarySerializer implements AutoCloseable {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final Path domainTmpFile;
    private final Path problemTmpFile;
    private final Path planTmpFile;

    /**
     * Serializes the non-null objects and makes them available at a temporary location.
     *
     * @param domain the domain to serialize, non-null
     * @param problem the problem to serialize, nullable
     * @param plan the plan to serialize, nullable
     * @throws IOException if an error during writing/reading the files occurs
     */
    public ExecutableTemporarySerializer(Domain domain, Problem problem, Plan plan) throws IOException {
        if (domain == null) {
            throw new IllegalArgumentException("Cannot serialize with a null domain.");
        }
        VariableDomainIO domainIO = new VariableDomainIO();
        DefaultProblemIO problemIO = new DefaultProblemIO(domain);
        DataWriter<Plan> planIO = null;
        if (problem != null) {
            if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
                planIO = new TemporalPlanIO(domain, problem);
            } else {
                planIO = new SequentialPlanIO(domain, problem);
            }
        }

        String serializedDomain = domainIO.serialize(domain);
        String serializedProblem = problem == null ? "" : problemIO.serialize(problem);
        String serializedPlan = planIO == null || plan == null ? "" : planIO.serialize(plan);
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

    /**
     * Get the path of the temporary domain file.
     *
     * @return the domain file
     */
    public Path getDomainTmpFile() {
        return domainTmpFile;
    }

    /**
     * Get the path of the temporary problem file.
     *
     * @return the problem file
     */
    public Path getProblemTmpFile() {
        return problemTmpFile;
    }

    /**
     * Get the path of the temporary plan file.
     *
     * @return the plan file
     */
    public Path getPlanTmpFile() {
        return planTmpFile;
    }

    @Override
    public void close() throws IOException {
        logger.debug("Deleting serialized files...");
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
