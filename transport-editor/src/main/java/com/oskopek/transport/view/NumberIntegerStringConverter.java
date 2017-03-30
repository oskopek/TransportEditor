package com.oskopek.transport.view;

import javafx.util.converter.IntegerStringConverter;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integer to String and back converter, used for inputting numbers. Does regex replacement internally for non-numerical
 * characters. Logs instead of throwing a number format exception. Defaults to 0 if no value could be parsed.
 */
public class NumberIntegerStringConverter extends IntegerStringConverter {

    private static final transient Logger logger = LoggerFactory.getLogger(NumberIntegerStringConverter.class);

    @Override
    public Integer fromString(String string) {
        if (string == null) {
            return 0;
        }
        String replaced = string.replaceAll("[^0-9]", "");
        return Try.of(() -> Integer.parseInt(replaced)).onFailure(e -> {
            if (e instanceof NumberFormatException) {
                logger.debug("Couldn't parse input into table (\"{}\") - NumberFormatException: ", replaced,
                        e.getMessage());
            } else {
                logger.debug("Couldn't parse input into table (\"{}\"): ", replaced, e);
            }
        }).getOrElse(0);
    }
}
