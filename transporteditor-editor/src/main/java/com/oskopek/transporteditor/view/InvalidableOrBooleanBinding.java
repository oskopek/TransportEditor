package com.oskopek.transporteditor.view;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable, invalidable or-binding. Can contain multiple boolean bindings as if joined by an OR operation.
 */
public class InvalidableOrBooleanBinding implements Binding<Boolean> {

    private final ObservableList<Binding<Boolean>> bindings;
    private final List<ChangeListener<? super Boolean>> changeListeners;
    private final List<InvalidationListener> invalidationListeners;

    /**
     * Singleton constructor.
     *
     * @param binding the first binding in the chain
     */
    public InvalidableOrBooleanBinding(Binding<Boolean> binding) {
        this.bindings = FXCollections.unmodifiableObservableList(FXCollections.singletonObservableList(binding));
        this.changeListeners = new ArrayList<>();
        this.invalidationListeners = new ArrayList<>();
    }

    /**
     * Copy constructor without keeping listeners.
     *
     * @param bindings the bindings
     */
    protected InvalidableOrBooleanBinding(List<Binding<Boolean>> bindings) {
        this.bindings = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(bindings));
        this.changeListeners = new ArrayList<>();
        this.invalidationListeners = new ArrayList<>();
        bindings.forEach(b -> b.addListener((observable, oldValue, newValue) -> changeListeners
                .forEach(c -> c.changed(observable, oldValue, newValue))));
        bindings.forEach(b -> b.addListener(observable -> invalidationListeners
                .forEach(i -> i.invalidated(observable))));
    }

    /**
     * Append constructor.
     *
     * @param binding the old bindings
     * @param newBinding the new binding
     */
    private InvalidableOrBooleanBinding(InvalidableOrBooleanBinding binding, Binding<Boolean> newBinding) {
        ObservableList<Binding<Boolean>> newBindings = FXCollections.observableArrayList(binding.bindings);
        newBindings.add(newBinding);
        this.bindings = FXCollections.unmodifiableObservableList(newBindings);
        this.changeListeners = new ArrayList<>(binding.changeListeners);
        this.invalidationListeners = new ArrayList<>(binding.invalidationListeners);

        newBinding.addListener((observable, oldValue, newValue) -> changeListeners
                .forEach(c -> c.changed(observable, oldValue, newValue)));
        newBinding.addListener(observable -> invalidationListeners.forEach(i -> i.invalidated(observable)));
    }

    @Override
    public boolean isValid() {
        return bindings.stream().map(Binding::getValue).reduce(Boolean.TRUE, Boolean::logicalAnd);
    }

    @Override
    public void invalidate() {
        bindings.forEach(Binding::invalidate);
    }

    @Override
    public ObservableList<?> getDependencies() {
        return bindings;
    }

    @Override
    public void dispose() {
        bindings.clear();
    }

    @Override
    public void addListener(ChangeListener<? super Boolean> listener) {
        changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }

    @Override
    public Boolean getValue() {
        return bindings.stream().map(Binding::getValue).reduce(Boolean.FALSE, Boolean::logicalOr);
    }

    /**
     * Shorthand for {@link #getValue()}.
     *
     * @return true iff {@code getValue()} is true
     */
    public Boolean get() {
        return getValue();
    }

    /**
     * Append another binding as if joined by an OR operation with the rest.
     *
     * @param binding the appended binding
     * @return a new {@code InvalidableOrBooleanBinding}
     */
    public InvalidableOrBooleanBinding or(Binding<Boolean> binding) {
        return new InvalidableOrBooleanBinding(this, binding);
    }

    /**
     * Copy or binding without keeping listeners.
     *
     * @return the new or binding
     */
    public InvalidableOrBooleanBinding copyWithoutListeners() {
        return new InvalidableOrBooleanBinding(bindings);
    }
}
