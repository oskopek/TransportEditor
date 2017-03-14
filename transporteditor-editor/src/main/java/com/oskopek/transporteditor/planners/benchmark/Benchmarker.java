package com.oskopek.transporteditor.planners.benchmark;

import com.oskopek.transporteditor.planners.benchmark.config.BenchmarkConfig;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;
import javaslang.control.Try;

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
     */
    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Arguments: benchmark_config_path");
        }

        String benchmarkConfigPath = args[0];
        BenchmarkConfig benchmarkConfig = Try.of(() -> BenchmarkConfig.from(benchmarkConfigPath)).getOrElseThrow(
                () -> new IllegalStateException("Couldn't parse benchmark config."));
        Benchmark benchmark = benchmarkConfig.toBenchmark();
        BenchmarkResults results = benchmark.benchmark(benchmarkConfig.getThreadCount());
        System.out.println(results.toJson());
    }
}
