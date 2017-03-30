package com.oskopek.transport.benchmark.config;

import com.oskopek.transport.benchmark.Benchmark;
import com.oskopek.transport.benchmark.data.BenchmarkResults;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

public class BenchmarkConfigIOIT {

    private final Path basePath
            = Paths.get("src/test/resources/com/oskopek/transport/benchmark/config/");
    private final Path simpleConfigPath = basePath.resolve("simple-benchmark-config.json");

    @Before
    public void setUp() throws Exception {
        System.setProperty("transport.root", basePath.toAbsolutePath().toString());
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty("transport.root");
    }

    @Test
    public void parse() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(simpleConfigPath);
        assertThat(config).isNotNull();
        assertThat(config.getDomain()).isNotNull();
        assertThat(config.getProblems()).hasSize(2);
        assertThat(config.getPlanners()).hasSize(2);
        assertThat(config.getPlanners().values().stream().map(BenchmarkConfig.PlannerConfig::getParams))
                .containsExactlyInAnyOrder((String) null, "{0} {1} --search astar(ff())");
        assertThat(config.getPlanners().values().stream().map(BenchmarkConfig.PlannerConfig::getClassName))
                .allMatch(s -> s.startsWith("com.oskopek.transport.planners."));
        assertThat(config.getPlanners().keySet())
                .containsExactlyInAnyOrder("FastDownAstar", "PrologBFS");
        assertThat(config.getScoreFunctionType()).isNotNull().isEqualTo(ScoreFunctionType.ACTION_COUNT);
        assertThat(config.getThreadCount()).isNull();
    }

    @Test
    public void parseWithThreadCount() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(basePath.resolve("simple-benchmark-config-threadcount.json"));
        assertThat(config).isNotNull();
        assertThat(config.getDomain()).isNotNull();
        assertThat(config.getProblems()).hasSize(2);
        assertThat(config.getPlanners()).hasSize(2);
        assertThat(config.getPlanners().values().stream().map(BenchmarkConfig.PlannerConfig::getParams))
                .containsExactlyInAnyOrder((String) null, "{0} {1} --search astar(ff())");
        assertThat(config.getPlanners().values().stream().map(BenchmarkConfig.PlannerConfig::getClassName))
                .allMatch(s -> s.startsWith("com.oskopek.transport.planners."));
        assertThat(config.getPlanners().keySet())
                .containsExactlyInAnyOrder("FastDownAstar", "PrologBFS");
        assertThat(config.getScoreFunctionType()).isNotNull().isEqualTo(ScoreFunctionType.ACTION_COUNT);
        assertThat(config.getThreadCount()).isNotNull().isEqualTo(2);
    }

    @Test
    @Ignore("Use internal planners for this test") // TODO un-ignore
    public void toBenchmark() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(simpleConfigPath);
        Benchmark benchmark = config.toBenchmark();
        assertThat(benchmark).isNotNull();
        Integer threadCount = config.getThreadCount();
        BenchmarkResults results = benchmark.benchmark(threadCount);
        // TODO: Verify that uses correct arguments and threads
        String plannerName = config.getPlanners().entrySet().stream().findAny().get().getKey();
        assertThat(results.getRunTable().at(0, 0).getPlanner().getName()).isEqualTo(plannerName);
        assertThat(config.getTimeout()).isEqualTo(15);
    }
}
