package com.oskopek.transporteditor.event;

import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * Enables an object to be observable for internal progress.
 */
public interface ProgressObservable {

    /**
     * The double property that is bound to a UI element showing progress.
     * Expects values either equal to {@code -1d} or in the range {@code [0, 1]}.
     *
     * @return the double property
     */
    ReadOnlyDoubleProperty progressProperty();

}
