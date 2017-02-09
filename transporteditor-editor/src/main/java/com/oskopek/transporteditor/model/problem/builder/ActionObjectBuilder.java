package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.ActionObject;

import java.util.function.Consumer;

public interface ActionObjectBuilder<T extends ActionObject> {

    T build();

    void from(T instance);

    void from(T instance, Consumer<? super T> updateFunction);

    void update();

}
