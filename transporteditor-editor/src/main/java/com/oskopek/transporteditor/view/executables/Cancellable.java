package com.oskopek.transporteditor.view.executables;

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
