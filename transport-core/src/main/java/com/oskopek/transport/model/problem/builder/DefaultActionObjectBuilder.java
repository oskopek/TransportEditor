package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.problem.DefaultActionObject;

import java.util.function.Consumer;

/**
 * Action object builder for {@link DefaultActionObject}s.
 *
 * @param <T> the type of the action object
 */
public abstract class DefaultActionObjectBuilder<T extends DefaultActionObject> implements ActionObjectBuilder<T> {

    private String name;
    private Consumer<? super T> updateFunction;

    /**
     * Default constructor.
     */
    public DefaultActionObjectBuilder() {
        // intentionally empty
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    @FieldLocalization(key = "name", priority = 0, editable = false)
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void from(T instance) {
        this.name = instance.getName();
    }

    @Override
    public void from(T instance, Consumer<? super T> updateCallback) {
        this.updateFunction = updateCallback;
        from(instance);
    }

    @Override
    public void update() {
        if (updateFunction != null) {
            updateFunction.accept(build());
        }
    }
}
