package com.oskopek.transporteditor.planners.benchmark;

import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.planners.benchmark.config.BenchmarkConfig;
import com.oskopek.transporteditor.planners.benchmark.data.BenchmarkResults;

import java.io.FileInputStream;
import java.io.FileReader;

public class Benchmarker {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Arguments: benchmark_config_path");
        }

        String benchmarkConfigPath = args[0];
        BenchmarkConfig benchmarkConfig = BenchmarkConfig.from(benchmarkConfigPath);
        Benchmark benchmark = benchmarkConfig.toBenchmark();
        BenchmarkResults results =  benchmark.benchmark(benchmarkConfig.getThreadCount());
        System.out.println(results.toJson());
    }
}
