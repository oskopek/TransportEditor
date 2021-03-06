package com.oskopek.transport.tools.executables;

/**
 * Simple log message listener.
 */
public interface LogListener {

    /**
     * Accept the given message. Should be (almost) non-blocking.
     *
     * @param logMessage the message to accept
     */
    void accept(String logMessage);

}
