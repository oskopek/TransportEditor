package com.oskopek.transporteditor.event;

import javafx.beans.value.ObservableValue;

/**
 * Enables an object to be observable for internal progress.
 */
public interface ProgressObservable {

    /**
     * The progress property that is bound to a UI element showing progress.
     * Expects double values either equal to {@code -1d} or in the range {@code [0, 1]}.
     *
     * @return the progress property
     */
    ObservableValue<? extends Number> progressProperty();

}
