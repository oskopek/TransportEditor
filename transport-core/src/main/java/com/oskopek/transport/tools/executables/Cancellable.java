package com.oskopek.transport.tools.executables;

/**
 * Represents a cancellable process.
 */
public interface Cancellable {

    /**
     * Request a cancellation of the process. Potentially blocking call.
     *
     * @return true iff the process was successfully canceled.
     */
    boolean cancel();

}
