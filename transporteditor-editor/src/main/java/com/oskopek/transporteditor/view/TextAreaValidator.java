package com.oskopek.transporteditor.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.function.Function;

public class TextAreaValidator {

    private final ObservableValue<String> inputText;
    private final BooleanProperty isValid = new SimpleBooleanProperty();
    private final Function<String, Boolean> validationFunction;

    public TextAreaValidator(ObservableValue<String> inputText, Function<String, Boolean> validationFunction) {
        this.inputText = inputText;
        this.validationFunction = validationFunction;
        inputText.addListener(
                ((observable, oldValue, newValue) -> this.isValid.set(this.validationFunction.apply(newValue))));
    }

    public TextAreaValidator listenTo(ObservableValue<?>... listenTo) {
        Arrays.stream(listenTo).forEach(item -> item.addListener(
                (observable, oldValue, newValue) -> this.isValid.set(validationFunction.apply(inputText.getValue()))));
        return this;
    }

    public boolean isValid() {
        return isValid.get();
    }

    public ReadOnlyBooleanProperty isValidProperty() {
        return isValid;
    }
}
