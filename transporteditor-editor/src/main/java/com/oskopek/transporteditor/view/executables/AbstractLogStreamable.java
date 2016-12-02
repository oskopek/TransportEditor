package com.oskopek.transporteditor.view.executables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractLogStreamable implements LogStreamable {

    private transient ObservableList<LogListener> logListenerList;

    public AbstractLogStreamable() {
        this.logListenerList = FXCollections.observableArrayList();
    }

    private synchronized List<LogListener> getLogListenerList() {
        return logListenerList;
    }

    public ObservableList<LogListener> getLogListenerListUnmodifiable() {
        return FXCollections.unmodifiableObservableList(logListenerList);
    }

    protected void forEach(Consumer<? super LogListener> consumer) {
        getLogListenerListUnmodifiable().forEach(consumer);
    }

    protected void log(String message) {
        forEach(l -> l.accept(message));
    }

    @Override
    public void subscribe(LogListener listener) {
        getLogListenerList().add(listener);
    }

    @Override
    public void unsubscribe(LogListener listener) {
        getLogListenerList().remove(listener);
    }

    protected Object readResolve() {
        logListenerList = FXCollections.observableArrayList();
        return this;
    }

}
