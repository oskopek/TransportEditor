package com.oskopek.transporteditor.view;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InvalidableOrBooleanBinding implements Binding<Boolean> {

    private ObservableList<BooleanBinding> bindings = FXCollections.observableArrayList();
    private ObservableList<ChangeListener<? super Boolean>> changeListeners = FXCollections.observableArrayList();
    private final ChangeListener<? super Boolean> changeListener
            = (observable, oldValue, newValue) -> getChangeListeners().forEach(
            c -> c.changed(observable, oldValue, newValue));
    private ObservableList<InvalidationListener> invalidationListeners = FXCollections.observableArrayList();
    private final InvalidationListener invalidationListener = observable -> getInvalidationListeners().forEach(
            i -> i.invalidated(observable));

    public InvalidableOrBooleanBinding(BooleanBinding binding) {
        addBinding(binding);
    }

    private synchronized void addBinding(BooleanBinding binding) {
        getBindings().add(binding);
        binding.addListener(changeListener);
        binding.addListener(invalidationListener);
    }

    private synchronized void removeBinding(BooleanBinding binding) {
        getBindings().remove(binding);
        binding.removeListener(changeListener);
        binding.removeListener(invalidationListener);
    }

    private synchronized ObservableList<BooleanBinding> getBindings() {
        return bindings;
    }

    private synchronized ObservableList<ChangeListener<? super Boolean>> getChangeListeners() {
        return changeListeners;
    }

    private synchronized ObservableList<InvalidationListener> getInvalidationListeners() {
        return invalidationListeners;
    }

    @Override
    public boolean isValid() {
        return getBindings().stream().map(BooleanBinding::get).reduce(Boolean.TRUE, Boolean::logicalAnd);
    }

    @Override
    public void invalidate() {
        getBindings().forEach(BooleanBinding::invalidate);
    }

    @Override
    public ObservableList<?> getDependencies() {
        return getBindings();
    }

    @Override
    public void dispose() {
        bindings.clear();
    }

    @Override
    public void addListener(ChangeListener<? super Boolean> listener) {
        getChangeListeners().add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> listener) {
        getChangeListeners().remove(listener);
    }

    @Override
    public Boolean getValue() {
        return getBindings().stream().map(BooleanExpression::getValue).reduce(Boolean.FALSE, Boolean::logicalOr);
    }

    public Boolean get() {
        return getValue();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        getInvalidationListeners().add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        getInvalidationListeners().remove(listener);
    }

    public InvalidableOrBooleanBinding or(BooleanBinding binding) {
        addBinding(binding);
        return this;
    }
}
