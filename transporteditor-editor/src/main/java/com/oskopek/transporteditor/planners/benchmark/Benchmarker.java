package com.oskopek.transporteditor.planners.benchmark;

import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.planners.benchmark.config.BenchmarkConfig;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import com.oskopek.transporteditor.planners.benchmark.report.ReportGenerator;
import javaslang.control.Try;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class for the benchmarker jar.
 */
public final class Benchmarker {

    /**
     * Empty constructor.
     */
    private Benchmarker() {
        // intentionally empty
    }

    /**
     * Parses the benchmark config from the first command line argument and executes it.
     *
     * @param args the command line arguments
     * @throws IOException if an error during parsing or reporting occurs
     */
    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("Arguments: benchmark_config_path benchmark_result_dir");
        }

        String benchmarkConfigFile = args[0];
        Path benchmarkConfigPath = Paths.get(benchmarkConfigFile);
        BenchmarkConfig benchmarkConfig = Try.of(() -> BenchmarkConfig.from(benchmarkConfigPath))
                .getOrElseThrow(e -> new IllegalStateException("Couldn't parse benchmark config.", e));
        Benchmark benchmark = benchmarkConfig.toBenchmark();
        BenchmarkResults results = benchmark.benchmark(benchmarkConfig.getThreadCount());

        String benchmarkResultDir = args[1];
        Path benchmarkResultDirPath = Paths.get(benchmarkResultDir);
        IOUtils.deleteDirectoryRecursively(benchmarkResultDirPath);
        Files.createDirectory(benchmarkResultDirPath);
        Files.copy(benchmarkConfigPath, benchmarkResultDirPath.resolve(benchmarkConfigPath.getFileName()));
        IOUtils.writeToFile(benchmarkResultDirPath.resolve("results.json"), results.toJson());

        ReportGenerator generator = new ReportGenerator();
        generator.populateReportersWithReflection();
        generator.generate(results, benchmarkResultDirPath);

    }
}
