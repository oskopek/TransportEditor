package com.oskopek.transporteditor.planners.benchmark.config;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.planners.benchmark.Benchmark;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BenchmarkConfigIOIT {

    private final String simpleConfigPath = "simple-benchmark-config.json";

    @Test
    public void parse() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(simpleConfigPath);
        assertThat(config).isNotNull();
        assertThat(config.getDomain()).isNotNull();
        assertThat(config.getProblems()).hasSize(1);
        assertThat(config.getPlanners()).hasSize(2);
        assertThat(config.getPlanners().values()).containsExactlyInAnyOrder((String) null, "--alias seq-sat-lama-2011");
        assertThat(config.getPlanners().keySet()).allMatch(s -> s.startsWith("com.oskopek.transporteditor.planners."));
        assertThat(config.getScoreFunctionType()).isNotNull().isEqualTo(ScoreFunctionType.ACTION_COUNT);
        assertThat(config.getThreadCount()).isNull();
    }

    @Test
    public void parseWithThreadCount() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from("simple-benchmark-config-threadcount.json");
        assertThat(config).isNotNull();
        assertThat(config.getDomain()).isNotNull();
        assertThat(config.getProblems()).hasSize(1);
        assertThat(config.getPlanners()).hasSize(2);
        assertThat(config.getPlanners().values()).containsExactlyInAnyOrder((String) null, "--alias seq-sat-lama-2011");
        assertThat(config.getPlanners().keySet()).allMatch(s -> s.startsWith("com.oskopek.transporteditor.planners."));
        assertThat(config.getScoreFunctionType()).isNotNull().isEqualTo(ScoreFunctionType.ACTION_COUNT);
        assertThat(config.getThreadCount()).isNotNull().isEqualTo(2);
    }

    @Test
    @Ignore("Use internal planners for this test") // TODO un-ignore
    public void toBenchmark() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(simpleConfigPath);
        Benchmark benchmark = config.toBenchmark();
        assertThat(benchmark).isNotNull();
    }
}