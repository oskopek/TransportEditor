package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.ActionObject;

public interface ActionObjectBuilder<T extends ActionObject> {

    T build();

    void from(T instance);

}
