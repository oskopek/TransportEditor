package com.oskopek.transport.benchmark.config;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.DefaultProblemIO;
import com.oskopek.transport.persistence.IOUtils;
import com.oskopek.transport.persistence.VariableDomainIO;
import com.oskopek.transport.validation.ValValidator;
import com.oskopek.transport.benchmark.Benchmark;
import com.oskopek.transport.benchmark.ScoreFunction;
import com.oskopek.transport.benchmark.data.BenchmarkMatrix;
import com.oskopek.transport.benchmark.data.ProblemInfo;
import javaslang.Function2;
import javaslang.Tuple;
import javaslang.collection.Stream;
import javaslang.control.Try;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration of the benchmark instance. To be used with JSON de/serialization.
 */
public final class BenchmarkConfig {

    private Integer timeout;
    private Integer threadCount;
    private Map<String, PlannerConfig> planners;
    private String domain;
    private Map<String, ProblemInfo> problems;
    private ScoreFunctionType scoreFunctionType;

    /**
     * Empty constructor.
     */
    private BenchmarkConfig() {
        // intentionally empty
    }

    /**
     * Create planner instances via reflection from the class names and parameters.
     * Supports only empty constructor planners or single string param constructor planners.
     *
     * @param plannerClassNamesAndParams the parsed class name - param map
     * @return a list of planners
     */
    private static List<Planner> toPlanners(Map<String, PlannerConfig> plannerClassNamesAndParams) {
        if (plannerClassNamesAndParams == null || plannerClassNamesAndParams.isEmpty()) {
            throw new IllegalArgumentException("No planners configured.");
        }

        List<Planner> planners = new ArrayList<>(plannerClassNamesAndParams.size());
        plannerClassNamesAndParams.forEach((plannerName, config) -> {
            Class<? extends Planner> planner = Try.of(() -> Class.forName(config.getClassName())).getOrElseThrow(
                    e -> new IllegalStateException("Couldn't find planner class: " + config.getClassName(), e))
                    .asSubclass(Planner.class);
            Planner instance;
            String parameters = config.getParams();
            if (parameters == null || parameters.isEmpty()) {
                instance = Try.of(planner::newInstance).getOrElseThrow(e -> new IllegalStateException(
                        "Couldn't instantiate planner with empty constructor: " + planner, e));
            } else {
                instance = Try.of(() -> planner.getConstructor(String.class)).flatMap(
                        c -> Try.of(() -> c.newInstance(parameters))).getOrElseThrow(e -> new IllegalStateException(
                        "Couldn't instantiate planner with parameters: " + planner + ", " + parameters, e));
            }
            instance.setName(plannerName);
            planners.add(instance);
        });
        return planners;
    }

    /**
     * Construct a configuration instance from the given config file path.
     * Also loads the linked files, so as to contain all IO to this method.
     *
     * @param configFilePath the config file path
     * @return the loaded benchmark config instance
     * @throws IOException if an error during loading occurs
     */
    public static BenchmarkConfig from(Path configFilePath) throws IOException {
        BenchmarkConfigIO io = new BenchmarkConfigIO();
        BenchmarkConfig config = io.parse(IOUtils.concatReadAllLines(new FileInputStream(configFilePath.toFile())));

        if (config.domain == null) {
            throw new IllegalArgumentException("Failed to parse the domain filename.");
        }

        config.domain = Stream.of(config.domain).map(
                domainFilePath -> Try.of(() -> IOUtils.concatReadAllLines(new FileInputStream(domainFilePath)))
                        .getOrElseThrow(
                                e -> new IllegalStateException("Failed to read domain file: " + domainFilePath, e)))
                .get();

        if (config.problems == null) {
            throw new IllegalArgumentException("Failed to parse problem filenames.");
        }

        config.problems = Stream.ofAll(config.problems.entrySet()).toMap(e -> Tuple.of(e.getKey(), e.getValue()))
                .mapValues(problemFilePath ->
                        problemFilePath.updateFileContents(Try.of(() ->
                                IOUtils.concatReadAllLines(new FileInputStream(problemFilePath.getFilePath())))
                                .getOrElseThrow(e ->
                                        new IllegalStateException("Failed to read problem file: " + problemFilePath,
                                                e))))
                .toJavaMap();
        return config;
    }

    /**
     * Get the timeoutPerBenchmark.
     *
     * @return the timeoutPerBenchmark
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Get the threadCount.
     *
     * @return the threadCount
     */
    public Integer getThreadCount() {
        return threadCount;
    }

    /**
     * Get the planners.
     *
     * @return the planners
     */
    public Map<String, PlannerConfig> getPlanners() {
        return planners;
    }

    /**
     * Get the domain.
     *
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Get the problems.
     *
     * @return the problems
     */
    public Map<String, ProblemInfo> getProblems() {
        return problems;
    }

    /**
     * Get the scoreFunctionType.
     *
     * @return the scoreFunctionType
     */
    public ScoreFunctionType getScoreFunctionType() {
        return scoreFunctionType;
    }

    /**
     * Creates a benchmark from the configuration instance.
     * Initialized the score function and planners, parses the domain and problems.
     *
     * @return the initialized benchmark instance
     */
    public Benchmark toBenchmark() {
        VariableDomainIO domainIO = new VariableDomainIO();
        Domain domain = domainIO.parse(this.domain);

        List<Problem> problems = new ArrayList<>(this.problems.size());
        DefaultProblemIO problemIO = new DefaultProblemIO(domain);
        this.problems.forEach((name, problemInfo) -> problems
                .add(problemIO.parse(problemInfo.getFileContents()).putName(name)));

        List<Planner> planners = toPlanners(this.planners);
        Function2<Problem, Planner, Boolean> skipFunction = (problem, planner) -> false; // TODO: add skipping support
        if (scoreFunctionType == null) {
            throw new IllegalArgumentException("No score function type present.");
        }
        ScoreFunction scoreFunction = scoreFunctionType.toScoreFunction();
        Map<String, Problem> problemNames = Stream.ofAll(problems)
                .toJavaMap(problem -> Tuple.of(problem.getName(), problem));
        Map<Problem, ProblemInfo> problemInfo = Stream.ofAll(this.problems.entrySet())
                .toJavaMap(t -> Tuple.of(problemNames.get(t.getKey()), t.getValue()));

        ValValidator valValidator = new ValValidator();
        if (!valValidator.isAvailable()) {
            throw new IllegalStateException("VAL is not available, exiting.");
        }

        return new Benchmark(new BenchmarkMatrix(domain, problems, planners, problemInfo, timeout), scoreFunction,
                skipFunction, valValidator);
    }

    /**
     * Simple data container for planner configuration de/serialization.
     */
    static class PlannerConfig {

        private String className = "";
        private String params;

        /**
         * Default empty constructor.
         */
        PlannerConfig() {
            // intentionally empty
        }

        /**
         * Get the planner class name.
         *
         * @return the planner class name
         */
        String getClassName() {
            return className;
        }

        /**
         * Get the planner's parameters.
         *
         * @return the parameters, may be null
         */
        String getParams() {
            return params;
        }

        /**
         * Set the planner class name.
         *
         * @param className the planner class name
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * Set the planner's parameters.
         *
         * @param params the parameters, may be null
         */
        public void setParams(String params) {
            this.params = params;
        }
    }

}
