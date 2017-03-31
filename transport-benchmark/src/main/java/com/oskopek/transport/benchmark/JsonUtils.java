package com.oskopek.transport.benchmark;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Set of util methods to be used with {@link com.fasterxml.jackson}.
 */
public final class JsonUtils {

    /**
     * Empty constructor.
     */
    private JsonUtils() {
        // intentionally empty
    }

    /**
     * Create a default {@link ObjectMapper} with some features enabled.
     *
     * @return the default object mapper instance
     */
    public static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        return mapper;
    }

}
