package com.oskopek.transport.report;

import com.oskopek.transport.benchmark.data.BenchmarkResults;
import com.oskopek.transport.benchmark.data.BenchmarkResultsIO;
import com.oskopek.transport.persistence.IOUtils;
import javaslang.Tuple;
import javaslang.Value;
import javaslang.collection.Stream;
import javaslang.control.Try;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The logic behind report generation. Uses the added reporters to generate a folder full of reports.
 */
public class ReportGenerator {

    private final List<Reporter> reporters = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String reportsFolderName;

    /**
     * Default constructor.
     * @param reportsFolderName the name of the generated reports folder
     */
    public ReportGenerator(String reportsFolderName) {
        this.reportsFolderName = reportsFolderName;
    }

    /**
     * Takes a benchmark result JSON file as an argument and adds all {@link Reporter}s on the classpath,
     * then calls {@link #generate(Path)}.
     *
     * @param args the command-line arguments
     * @throws IOException if report generation fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Args: [resultFile.json] ([report-folder-name])");
        }

        String reportsFolderName = "reports";
        if (args.length == 2) {
                reportsFolderName = args[1];
        }
        Path resultFile = Paths.get(args[0]);
        ReportGenerator generator = new ReportGenerator(reportsFolderName);
        generator.populateReportersWithReflection();
        generator.generate(resultFile);
    }

    /**
     * Add all {@link Reporter}s found on the classpath.
     */
    public void populateReportersWithReflection() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClass(Reporter.class))
                .setScanners(new SubTypesScanner(false))
                .filterInputsBy(s -> s != null && s.startsWith("com.oskopek.transport.") && s.endsWith(".class"))
        );
        Stream.ofAll(reflections.getSubTypesOf(Reporter.class))
                .filter(type -> !Modifier.isAbstract(type.getModifiers()))
                .map(type -> Try.of(type::newInstance).toJavaOptional())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addReporter);
    }

    /**
     * Generate a folder full of reports from the {@code resultFile}, using the added {@link Reporter}s.
     * The report folder is located in the same directory as the {@code resultFile} and called {@code reports/}.
     *
     * @param resultFile the benchmark result JSON file
     * @throws IOException if an error during generation occurs
     */
    public void generate(Path resultFile) throws IOException {
        String resultFileContents = IOUtils.concatReadAllLines(Files.newInputStream(resultFile));
        BenchmarkResults results = new BenchmarkResultsIO().parse(resultFileContents);
        results = recalculateQuality(results);
        generate(results, resultFile.getParent());
    }

    private BenchmarkResults recalculateQuality(BenchmarkResults results) {
        Map<String, List<BenchmarkResults.JsonRun>> runs = Stream.ofAll(results.getRuns()).groupBy(r -> r.getProblem())
                .mapValues(Value::toJavaList).toJavaMap();
        Map<String, Double> bestScore = Stream.ofAll(runs.entrySet()).map(e -> Tuple.of(e.getKey(), e.getValue().stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getResults().getScore(), r.getResults().getBestScore()))
                .filter(Objects::nonNull).min(Double::compare)))
                .toJavaMap(t -> Tuple.of(t._1, t._2.orElse(null)));
        return BenchmarkResults.from(results.getRuns().stream()
                .map(r -> BenchmarkResults.JsonRun.of(r, r.getResults().updateBestScore(bestScore.get(r.getProblem()))))
                .collect(Collectors.toList()));
    }

    /**
     * Generate a folder full of reports from the {@code results} using the added {@link Reporter}s.
     * The report folder is located in the {@code resultDir} and called {@code reports/}.
     *
     * @param results the results to generate reports from
     * @param resultDir the directory the results are contained in (reports will be a subdirectory)
     * @throws IOException if an error during generation occurs
     */
    public void generate(BenchmarkResults results, Path resultDir) throws IOException {
        Path reportDir = resultDir.resolve(reportsFolderName);
        try {
            Files.createDirectory(reportDir);
        } catch (FileAlreadyExistsException e) {
            logger.warn("Report directory already exists, will possibly overwrite files.");
        }

        reporters.stream().map(reporter -> Tuple.of(reporter.getFileName(), reporter.generateReport(results.getRuns())))
                .forEach(tuple -> {
                    String reportName = tuple._1;
                    logger.info("Generating report: {}", reportName);
                    Path reportFile = reportDir.resolve(reportName);
                    Try.run(() -> IOUtils.writeToFile(reportFile, tuple._2))
                            .onFailure(e -> new IllegalStateException("Failed to persist report: " + reportFile, e));
                });
    }

    /**
     * Add a reporter to use when generating.
     *
     * @param reporter the reporter to add
     */
    public void addReporter(Reporter reporter) {
        reporters.add(reporter);
    }

}
