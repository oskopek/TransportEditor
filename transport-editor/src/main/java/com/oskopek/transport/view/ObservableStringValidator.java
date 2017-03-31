package com.oskopek.transport.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Simple predicate text area validator that exposes a boolean "is valid" property.
 */
public class ObservableStringValidator {

    private final ObservableValue<String> inputText;
    private final BooleanProperty isValid;
    private final Predicate<String> predicate;

    /**
     * Default constructor and adds a listener to update the {@link #isValid} property.
     *
     * @param inputText the observable text property to validate against
     * @param predicate the validation predicate
     */
    public ObservableStringValidator(ObservableValue<String> inputText, Predicate<String> predicate) {
        this.inputText = inputText;
        this.isValid = new SimpleBooleanProperty(predicate.test(inputText.getValue()));
        this.predicate = predicate;
        inputText.addListener(
                ((observable, oldValue, newValue) -> this.isValid.set(this.predicate.test(newValue))));
    }

    /**
     * Add "invalidation" listeners to each observable value.
     *
     * @param listenTo the observable values to listen to for re-testing the string validity
     * @return this reference
     */
    public ObservableStringValidator revalidateWhenChanged(ObservableValue<?>... listenTo) {
        Arrays.stream(listenTo).forEach(item -> item.addListener(
                (observable, oldValue, newValue) -> this.isValid.set(predicate.test(inputText.getValue()))));
        return this;
    }

    /**
     * Is the referenced observable valid.
     *
     * @return true iff it is valid
     */
    public boolean isValid() {
        return isValid.get();
    }

    /**
     * The valid property.
     *
     * @return the valid property
     */
    public ReadOnlyBooleanProperty isValidProperty() {
        return isValid;
    }
}
