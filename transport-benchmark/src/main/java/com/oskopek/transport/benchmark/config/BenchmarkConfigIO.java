package com.oskopek.transport.benchmark.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oskopek.transport.persistence.DataIO;
import com.oskopek.transport.benchmark.JsonUtils;
import javaslang.control.Try;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Serializer/parser for {@link BenchmarkConfig}.
 */
public class BenchmarkConfigIO implements DataIO<BenchmarkConfig> {

    private final ObjectMapper mapper;

    /**
     * Default constructor.
     */
    public BenchmarkConfigIO() {
        mapper = JsonUtils.createDefaultMapper();
    }

    @Override
    public BenchmarkConfig parse(String contents) {
        return Try.of(() -> mapper.readValue(StrSubstitutor.replaceSystemProperties(contents), BenchmarkConfig.class))
                .getOrElseThrow(e -> new IllegalStateException("Error while parsing benchmark JSON.", e));
    }

    @Override
    public <T extends BenchmarkConfig> String serialize(T object) {
        return Try.of(() -> mapper.writeValueAsString(object)).getOrElseThrow(
                e -> new IllegalStateException("Error while serializing benchmark JSON.", e));
    }
}
