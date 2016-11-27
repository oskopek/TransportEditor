package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.view.EnterStringDialogPaneCreator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller for choosing a course out of several choices.
 */
public class VariableDomainController extends AbstractController {

    private Dialog<ButtonType> dialog;

    @FXML
    private RadioButton sequentialRadio;

    @FXML
    private RadioButton temporalRadio;

    @FXML
    private CheckBox fuelCheck;

    @FXML
    private CheckBox numericCheck;

    @FXML
    private CheckBox capacityCheck;

    @FXML
    private TextArea goalArea;

    @FXML
    private TextArea metricArea;

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

    private ToggleGroup group = new ToggleGroup();

    @FXML
    private void initialize() {
        sequentialRadio.setToggleGroup(group);
        temporalRadio.setToggleGroup(group);

        numericLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        numericCheck.disableProperty().bind(group.selectedToggleProperty().isNull());
        capacityLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        capacityCheck.disableProperty().bind(group.selectedToggleProperty().isNull());
        fuelLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        fuelCheck.disableProperty().bind(group.selectedToggleProperty().isNull());

        metricArea.disableProperty().bind(numericCheck.selectedProperty().not());
        metricLabel.disableProperty().bind(numericCheck.selectedProperty().not());
        goalArea.disableProperty().bind(numericCheck.selectedProperty().not());
        goalLabel.disableProperty().bind(numericCheck.selectedProperty().not());
    }

    /**
     * Handles submitting the dialog in case the presses enter into the found course table.
     *
     * @param event the generated event
     */
    @FXML
    private void handleOnKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            applyDialog();
        }
    }

    /**
     * Closes the dialog as if the "Apply" button was clicked.
     */
    private void applyDialog() {
        dialog.resultProperty().setValue(ButtonType.APPLY);
        dialog.close();
    }

    /**
     * Get the dialog (used for reporting double clicks in the table).
     *
     * @return the dialog
     */
    public Dialog<ButtonType> getDialog() {
        return dialog;
    }

    /**
     * Set the dialog (used for reporting double clicks in the table).
     *
     * @param dialog the dialog wrapper for {@link EnterStringDialogPaneCreator}
     */
    public void setDialog(Dialog<ButtonType> dialog) {
        this.dialog = dialog;
    }

    /**
     * Get the string the user submitted.
     *
     * @return the submitted string
     */
    public VariableDomain getChosenDomain() {
        throw new IllegalStateException("Not implemented yet");
    } // TODO: Implement me + FXML
}
