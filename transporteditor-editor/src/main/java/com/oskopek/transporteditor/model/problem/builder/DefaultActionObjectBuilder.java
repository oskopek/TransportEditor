package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.DefaultActionObject;

public class DefaultActionObjectBuilder<T extends DefaultActionObject> implements ActionObjectBuilder<T> {

    private String name;

    public DefaultActionObjectBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "vdcreator.name", priority = 0)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public T build() {
        return (T) new DefaultActionObject(getName());
    }

    @Override
    public void from(T instance) {
        setName(instance.getName());
    }
}
