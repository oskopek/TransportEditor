package com.oskopek.transporteditor.planners.benchmark.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oskopek.transporteditor.persistence.DataIO;
import javaslang.control.Try;

public class BenchmarkResultsIO implements DataIO<BenchmarkResults> {

    private final ObjectMapper mapper;

    public BenchmarkResultsIO() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public BenchmarkResults parse(String contents) {
        return Try.of(() -> mapper.readValue(contents, BenchmarkResults.class))
                .getOrElseThrow(e -> new IllegalStateException("Error while parsing benchmark result JSON.", e));
    }

    @Override
    public <T extends BenchmarkResults> String serialize(T object) {
        return Try.of(() -> mapper.writeValueAsString(object))
                .getOrElseThrow(e -> new IllegalStateException("Error while serializing benchmark result JSON.", e));
    }
}
