package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.DefaultActionObject;

import java.util.function.Consumer;

public class DefaultActionObjectBuilder<T extends DefaultActionObject> implements ActionObjectBuilder<T> {

    private String name;
    private Consumer<? super T> updateFunction;

    public DefaultActionObjectBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "name", priority = 0)
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

    @Override
    public void from(T instance, Consumer<? super T> updateFunction) {
        this.updateFunction = updateFunction;
        from(instance);
    }

    @Override
    public void update() {
        if (updateFunction != null) {
            updateFunction.accept(build());
        }
    }
}
