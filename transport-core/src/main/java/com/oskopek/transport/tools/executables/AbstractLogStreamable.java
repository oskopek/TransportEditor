package com.oskopek.transport.tools.executables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Consumer;

/**
 * A simple list-backed implementation of {@link LogStreamable}.
 * Is actually non-abstract, but lacks any log-worthy activity.
 */
public abstract class AbstractLogStreamable implements LogStreamable {

    private final transient ObservableList<LogListener> logListenerList;

    /**
     * Default constructor.
     */
    public AbstractLogStreamable() {
        this.logListenerList = FXCollections.observableArrayList();
    }

    /**
     * A synchronized accessor to the log listener list.
     *
     * @return the log listener list
     */
    private List<LogListener> getLogListenerList() {
        synchronized (logListenerList) {
            return logListenerList;
        }
    }

    /**
     * Get an unmodifiable version of the observable log listener list.
     *
     * @return the log listener list
     */
    public ObservableList<LogListener> getLogListenerListUnmodifiable() {
        return FXCollections.unmodifiableObservableList(logListenerList);
    }

    /**
     * For each log listener in list.
     *
     * @param consumer the action to apply to the log listeners
     */
    protected void forEach(Consumer<? super LogListener> consumer) {
        getLogListenerListUnmodifiable().forEach(consumer);
    }

    /**
     * Delegate the message to all subscribed log listeners.
     *
     * @param message the message to delegate
     */
    protected void log(String message) {
        forEach(l -> l.accept(message + '\n'));
    }

    @Override
    public void subscribe(LogListener listener) {
        getLogListenerList().add(listener);
    }

    @Override
    public void unsubscribe(LogListener listener) {
        getLogListenerList().remove(listener);
    }

}
