package com.oskopek.transport.controller;

import com.oskopek.transport.model.domain.DomainType;
import com.oskopek.transport.model.domain.VariableDomain;
import com.oskopek.transport.persistence.VariableDomainBuilder;
import com.oskopek.transport.view.ObservableStringValidator;
import com.oskopek.transport.view.ExecutableParametersCreator;
import com.oskopek.transport.view.ValidationProperty;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for choosing a course out of several choices.
 */
public class VariableDomainController extends AbstractController {

    private final VariableDomainBuilder domainBuilder = new VariableDomainBuilder();
    private final ToggleGroup group = new ToggleGroup();
    private final BooleanProperty radioButtonsValid = new SimpleBooleanProperty();
    private Stage dialog;
    @FXML
    private Label headerText;
    @FXML
    private TextField nameField;
    private ValidationProperty nameFieldValid;
    @FXML
    private RadioButton sequentialRadio;
    private ValidationProperty sequentialRadioValid;
    @FXML
    private RadioButton temporalRadio;
    private ValidationProperty temporalRadioValid;
    @FXML
    private CheckBox fuelCheck;
    private ValidationProperty fuelCheckValid;
    @FXML
    private CheckBox numericCheck;
    private ValidationProperty numericCheckValid;
    @FXML
    private TextArea goalArea;
    private ValidationProperty goalAreaValid;
    @FXML
    private TextArea metricArea;
    private ValidationProperty metricAreaValid;
    @FXML
    private CheckBox capacityCheck;
    private ValidationProperty capacityCheckValid;
    @FXML
    private Label goalLabel;
    @FXML
    private Label metricLabel;
    @FXML
    private Label fuelLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label numericLabel;
    @FXML
    private Button applyButton;
    @FXML
    private Button cancelButton;
    private ButtonBar.ButtonData result;
    private BooleanBinding allValidationsValid;

    /**
     * JavaFX initializer method. Registers with the event bus. Initializes button disabling
     * and other validation.
     */
    @FXML
    private void initialize() {
        nameFieldValid = new ValidationProperty(messages.getString("vdcreator.valid.name"),
                nameField);
        sequentialRadioValid = new ValidationProperty(messages.getString("vdcreator.valid.domainType"),
                sequentialRadio);
        temporalRadioValid = new ValidationProperty(messages.getString("vdcreator.valid.domainType"), temporalRadio);
        fuelCheckValid = new ValidationProperty(messages.getString("vdcreator.valid.fuelCheck"), fuelCheck);
        numericCheckValid = new ValidationProperty(messages.getString("vdcreator.valid.numericCheck"), numericCheck);
        goalAreaValid = new ValidationProperty(messages.getString("vdcreator.valid.goalArea"), goalArea);
        metricAreaValid = new ValidationProperty(messages.getString("vdcreator.valid.metricArea"), metricArea);
        capacityCheckValid = new ValidationProperty(messages.getString("vdcreator.valid.capacity"), capacityCheck);

        nameFieldValid.bind(nameField.textProperty().isNotEmpty());

        sequentialRadioValid.bind(radioButtonsValid);
        temporalRadioValid.bind(radioButtonsValid);
        radioButtonsValid.bind(group.selectedToggleProperty().isNotNull());

        fuelCheckValid.set(true);
        capacityCheckValid.set(true);
        numericCheckValid.set(true);

        goalAreaValid.bind(new ObservableStringValidator(goalArea.textProperty(),
                s -> goalArea.isDisabled() || !s.isEmpty())
                .revalidateWhenChanged(goalArea.disabledProperty()).isValidProperty()); // TODO goal area validation
        metricAreaValid.bind(new ObservableStringValidator(metricArea.textProperty(),
                s -> metricArea.isDisabled() || !s.isEmpty())
                .revalidateWhenChanged(metricArea.disabledProperty()).isValidProperty()); // TODO metric area validation

        allValidationsValid = nameFieldValid.and(sequentialRadioValid).and(temporalRadioValid).and(fuelCheckValid)
                .and(numericCheckValid)
                .and(goalAreaValid).and(metricAreaValid).and(capacityCheckValid);
        applyButton.disableProperty().bind(allValidationsValid.not());

        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.APPLY);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);

        domainBuilder.nameProperty().bind(nameField.textProperty());

        sequentialRadio.setToggleGroup(group);
        sequentialRadio.setUserData(DomainType.Sequential);
        temporalRadio.setToggleGroup(group);
        temporalRadio.setUserData(DomainType.Temporal);
        group.selectedToggleProperty().addListener(
                e -> domainBuilder.setDomainType((DomainType) group.getSelectedToggle().getUserData()));

        domainBuilder.capacityProperty().bind(capacityCheck.selectedProperty());
        domainBuilder.fuelProperty().bind(fuelCheck.selectedProperty());
        domainBuilder.numericProperty().bind(numericCheck.selectedProperty());

        // TODO: Metric and goal parsing
        // domainBuilder.goalTextProperty().bind(goalArea.textProperty());
        // domainBuilder.metricTextProperty().bind(metricArea.textProperty());

        ReadOnlyBooleanWrapper disableBind = new ReadOnlyBooleanWrapper(true);
        numericLabel.disableProperty().bind(group.selectedToggleProperty().isNull().or(disableBind)); // TODO re-enable
        numericCheck.disableProperty().bind(group.selectedToggleProperty().isNull().or(disableBind)); // TODO re-enable
        capacityLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        capacityCheck.disableProperty().bind(group.selectedToggleProperty().isNull());
        fuelLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        fuelCheck.disableProperty().bind(group.selectedToggleProperty().isNull());

        metricArea.disableProperty().bind(numericCheck.selectedProperty().not().or(disableBind)); // TODO re-enable
        metricLabel.disableProperty().bind(numericCheck.selectedProperty().not().or(disableBind)); // TODO re-enable
        goalArea.disableProperty().bind(numericCheck.selectedProperty().not().or(disableBind)); // TODO re-enable
        goalLabel.disableProperty().bind(numericCheck.selectedProperty().not().or(disableBind)); // TODO re-enable
    }

    /**
     * Set the header text.
     *
     * @param headerText the text to set
     */
    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    /**
     * Get the name text field.
     *
     * @return the text field
     */
    public TextField getNameField() {
        return nameField;
    }

    /**
     * Handles the Apply button press.
     */
    @FXML
    private void handleApplyButton() {
        if (allValidationsValid.get()) {
            result = ButtonBar.ButtonData.APPLY;
            dialog.close();
        } else {
            throw new IllegalStateException("Button should be disabled if validation failed!");
        }
    }

    /**
     * Handles the Cancel button press.
     */
    @FXML
    private void handleCancelButton() {
        result = ButtonBar.ButtonData.CANCEL_CLOSE;
        dialog.close();
    }

    /**
     * Set the dialog (used for reporting double clicks in the table).
     *
     * @param dialog the dialog wrapper for {@link ExecutableParametersCreator}
     */
    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

    /**
     * Get the domain the user submitted.
     *
     * @return the submitted domain, null if cancelled
     */
    public VariableDomain getChosenDomain() {
        return result != null && ButtonBar.ButtonData.APPLY.equals(result) ? domainBuilder.toDomain() : null;
    }
}
