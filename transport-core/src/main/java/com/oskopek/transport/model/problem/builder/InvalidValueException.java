package com.oskopek.transport.model.problem.builder;

/**
 * Simple wrapper exception for signalizing invalid action builder property values.
 */
public class InvalidValueException extends Exception {

    /**
     * Default constructor.
     *
     * @param message the message, preferably language-agnostic
     */
    public InvalidValueException(String message) {
        super(message);
    }

}
