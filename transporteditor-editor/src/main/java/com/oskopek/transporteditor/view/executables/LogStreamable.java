package com.oskopek.transporteditor.view.executables;

public interface LogStreamable {

    void subscribe(LogListener listener);

    void unsubscribe(LogListener listener);

}
