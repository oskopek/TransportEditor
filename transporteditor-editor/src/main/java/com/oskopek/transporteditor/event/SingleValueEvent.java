package com.oskopek.transporteditor.event;

public abstract class SingleValueEvent<T> {

    private final T value;

    public SingleValueEvent(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
