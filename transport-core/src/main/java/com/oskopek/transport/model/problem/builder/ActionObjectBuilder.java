package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.problem.ActionObject;

import java.util.function.Consumer;

/**
 * Represents a mutable builder for action objects.
 *
 * @param <T> the action object type
 */
public interface ActionObjectBuilder<T extends ActionObject> {

    /**
     * Build the action object from properties of the builder.
     *
     * @return the built action object
     * @throws InvalidValueException if a property's value was invalid
     */
    T build() throws InvalidValueException;

    /**
     * Initialize all related properties of the builder from the action object.
     *
     * @param instance the action object instance
     */
    void from(T instance);

    /**
     * Initialize all related properties of the builder from the action object and set the update callback.
     *
     * @param instance the action object instance
     * @param updateCallback the update callback
     */
    void from(T instance, Consumer<? super T> updateCallback);

    /**
     * Execute the update callback with the built action object.
     * @throws InvalidValueException if a property's value was invalid
     */
    void update() throws InvalidValueException;

}
