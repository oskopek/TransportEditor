package com.oskopek.transporteditor.validation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

public class TextAreaValidator {

    private final ObservableValue<String> inputText;
    private final BooleanProperty isValid = new SimpleBooleanProperty();

    public TextAreaValidator(ObservableValue<String> inputText, Function<String, Boolean> isValid) {
        this.inputText = inputText;
        inputText.addListener(((observable, oldValue, newValue) -> this.isValid.set(isValid.apply(newValue))));
    }

    public boolean isValid() {
        return isValid.get();
    }

    public ReadOnlyBooleanProperty isValidProperty() {
        return isValid;
    }
}
