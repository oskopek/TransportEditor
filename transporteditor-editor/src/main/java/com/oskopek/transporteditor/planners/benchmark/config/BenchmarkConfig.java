package com.oskopek.transporteditor.planners.benchmark.config;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.DefaultProblemIO;
import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.persistence.VariableDomainIO;
import com.oskopek.transporteditor.planners.benchmark.Benchmark;
import com.oskopek.transporteditor.planners.benchmark.ScoreFunction;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkMatrix;
import javaslang.Function2;
import javaslang.collection.Stream;
import javaslang.control.Try;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration of the benchmark instance. To be used with JSON de/serialization.
 */
public final class BenchmarkConfig {

    private Integer threadCount;
    private Map<String, String> planners;
    private String domain;
    private List<String> problems;
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
    private static List<Planner> toPlanners(Map<String, String> plannerClassNamesAndParams) {
        if (plannerClassNamesAndParams == null || plannerClassNamesAndParams.isEmpty()) {
            throw new IllegalArgumentException("No planners configured.");
        }

        List<Planner> planners = new ArrayList<>();
        plannerClassNamesAndParams.forEach((className, parameters) -> {
            Class<? extends Planner> planner = Try.of(() -> Class.forName(className)).getOrElseThrow(
                    e -> new IllegalStateException("Couldn't find planner class: " + className, e)).asSubclass(
                    Planner.class);
            Planner instance;
            if (parameters == null || parameters.isEmpty()) {
                instance = Try.of(() -> planner.newInstance()).getOrElseThrow(e -> new IllegalStateException(
                        "Couldn't instantiate planner with empty constructor: " + planner, e));
            } else {
                instance = Try.of(() -> planner.getConstructor(String.class)).flatMap(
                        c -> Try.of(() -> c.newInstance(parameters))).getOrElseThrow(e -> new IllegalStateException(
                        "Couldn't instantiate planner with parameters: " + planner + ", " + parameters, e));
            }
            planners.add(instance);
        });
        return planners;
    }

    /**
     * Construct a configuration instance from the given config file path.
     * Also loads the linked files, so as to contain all IO to this method.
     *
     * @param configFile the config file path
     * @return the loaded benchmark config instance
     * @throws IOException if an error during loading occurs
     */
    public static BenchmarkConfig from(String configFile) throws IOException {
        Path configFilePath = Paths.get(configFile);
        Path directory = configFilePath.getParent();

        BenchmarkConfigIO io = new BenchmarkConfigIO();
        BenchmarkConfig config = io.parse(IOUtils.concatReadAllLines(new FileInputStream(configFilePath.toFile())));

        if (config.domain == null) {
            throw new IllegalArgumentException("Failed to parse the domain filename.");
        }

        config.domain = Stream.of(config.domain).map(
                domainFilePath -> Try.of(() -> IOUtils.concatReadAllLines(new FileInputStream(domainFilePath)))
                        .getOrElseThrow(
                                () -> new IllegalStateException("Failed to read domain file: " + domainFilePath)))
                .get();

        if (config.problems == null) {
            throw new IllegalArgumentException("Failed to parse problem filenames.");
        }

        config.problems = config.problems.stream().map(
                problemFilePath -> Try.of(() -> IOUtils.concatReadAllLines(new FileInputStream(problemFilePath)))
                        .getOrElseThrow(
                                () -> new IllegalStateException("Failed to read problem file: " + problemFilePath)))
                .collect(Collectors.toList());

        return config;
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
    public Map<String, String> getPlanners() {
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
    public List<String> getProblems() {
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

        List<Problem> problems = new ArrayList<>();
        DefaultProblemIO problemIO = new DefaultProblemIO(domain);
        for (String problemFileContents : this.problems) {
            problems.add(problemIO.parse(problemFileContents));
        }

        List<Planner> planners = toPlanners(this.planners);
        Function2<Problem, Planner, Boolean> skipFunction = (problem, planner) -> false; // TODO: add skipping support
        if (scoreFunctionType == null) {
            throw new IllegalArgumentException("No score function type present.");
        }
        ScoreFunction scoreFunction = scoreFunctionType.toScoreFunction();
        return new Benchmark(new BenchmarkMatrix(domain, problems, planners), scoreFunction, skipFunction);
    }

}
