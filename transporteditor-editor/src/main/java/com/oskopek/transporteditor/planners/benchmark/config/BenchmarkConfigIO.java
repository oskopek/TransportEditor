package com.oskopek.transporteditor.planners.benchmark.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oskopek.transporteditor.persistence.DataIO;
import com.oskopek.transporteditor.planners.benchmark.Benchmark;
import javaslang.control.Try;

public class BenchmarkConfigIO implements DataIO<BenchmarkConfig> {

    private final ObjectMapper mapper;

    public BenchmarkConfigIO() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public BenchmarkConfig parse(String contents) {
        return Try.of(() -> mapper.readValue(contents, BenchmarkConfig.class))
                .getOrElseThrow(e -> new IllegalStateException("Error while parsing benchmark JSON.", e));
    }

    @Override
    public <T extends BenchmarkConfig> String serialize(T object) {
        return Try.of(() -> mapper.writeValueAsString(object))
                .getOrElseThrow(e -> new IllegalStateException("Error while serializing benchmark JSON.", e));
    }
}
