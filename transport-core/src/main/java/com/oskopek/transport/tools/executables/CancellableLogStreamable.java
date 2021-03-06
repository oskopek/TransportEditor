package com.oskopek.transport.tools.executables;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple JavaFX motivated cancellable extension of {@link AbstractLogStreamable}.
 * Used for checking the {@link #shouldCancel()} and stopping in case it is true.
 */
public abstract class CancellableLogStreamable extends AbstractLogStreamable implements Cancellable {

    private final transient BooleanProperty shouldCancelProperty = new SimpleBooleanProperty(false);
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Test if we should cancel.
     *
     * @return true iff we should cancel the activity
     */
    protected boolean shouldCancel() {
        synchronized (this.shouldCancelProperty) {
            return shouldCancelProperty.get();
        }
    }

    /**
     * Set the shouldCancel property.
     *
     * @param shouldCancel should cancel?
     */
    protected void setShouldCancel(boolean shouldCancel) {
        synchronized (this.shouldCancelProperty) {
            this.shouldCancelProperty.setValue(shouldCancel);
        }
    }

    @Override
    public boolean cancel() {
        logger.debug("Cancelling...");
        setShouldCancel(true);
        return true;
    }
}
