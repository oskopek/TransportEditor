package com.oskopek.transporteditor.planners.benchmark.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oskopek.transporteditor.persistence.DataIO;
import javaslang.control.Try;
import org.apache.commons.lang3.text.StrSubstitutor;

public class BenchmarkConfigIO implements DataIO<BenchmarkConfig> {

    private final ObjectMapper mapper;

    public BenchmarkConfigIO() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
    }

    @Override
    public BenchmarkConfig parse(String contents) {
        return Try.of(() -> mapper.readValue(StrSubstitutor.replaceSystemProperties(contents), BenchmarkConfig.class))
                .getOrElseThrow(e -> new IllegalStateException("Error while parsing benchmark JSON.", e));
    }

    @Override
    public <T extends BenchmarkConfig> String serialize(T object) {
        return Try.of(() -> mapper.writeValueAsString(object))
                .getOrElseThrow(e -> new IllegalStateException("Error while serializing benchmark JSON.", e));
    }
}
