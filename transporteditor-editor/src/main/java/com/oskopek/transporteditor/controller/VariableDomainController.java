package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.domain.VariableDomainBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for choosing a course out of several choices.
 */
public class VariableDomainController extends AbstractController {

    private final VariableDomainBuilder domainBuilder = new VariableDomainBuilder();
    private final ToggleGroup group = new ToggleGroup();
    private Stage dialog;
    @FXML
    private Label headerText;
    @FXML
    private TextField nameField;
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
    @FXML
    private ButtonBar buttonBar;
    private ButtonBar.ButtonData result;

    @FXML
    private void initialize() {
        Button applyButton = new Button(messages.getString("vdcreator.apply"));
        applyButton.setDefaultButton(true);
        Button cancelButton = new Button(messages.getString("vdcreator.cancel"));
        applyButton.setCancelButton(true);
        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.APPLY);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(applyButton, cancelButton);

        domainBuilder.nameProperty().bind(nameField.textProperty());

        sequentialRadio.setToggleGroup(group);
        sequentialRadio.setUserData(PddlLabel.ActionCost);
        temporalRadio.setToggleGroup(group);
        temporalRadio.setUserData(PddlLabel.Temporal);
        group.selectedToggleProperty().addListener(e -> {
            Toggle selected = group.getSelectedToggle();
            PddlLabel label = (PddlLabel) selected.getUserData();
            // TODO: Ended here
        });

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

    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    public TextField getNameField() {
        return nameField;
    }

    /**
     * Set the dialog (used for reporting double clicks in the table).
     *
     * @param dialog the dialog wrapper for {@link com.oskopek.transporteditor.view.VariableDomainCreator}
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
