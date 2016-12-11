package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.view.ExecutableParametersCreator;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controller for choosing a course out of several choices.
 */
public class LogProgressController extends AbstractController {

    private Stage dialog;

    @FXML
    private Label headerText;

    @FXML
    private TextArea logArea;

    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    private ButtonBar.ButtonData result;

    @FXML
    private void initialize() {
        logArea.setStyle("-fx-border-color: transparent");

        ButtonBar.setButtonData(okButton, ButtonBar.ButtonData.OK_DONE);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    public void setProgressConditions(ObservableValue<Boolean> successfullyFinished,
            ObservableValue<Boolean> cancelAvailable) {
        okButton.disableProperty().bind(successfullyFinished);
        cancelButton.disableProperty().bind(cancelAvailable);
    }

    public synchronized void appendLog(String logMessage) {
        logArea.appendText(logMessage);
    }

    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

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

    @FXML
    private void handleOkButton(ActionEvent actionEvent) {
        if (!okButton.isDisabled()) {
            result = ButtonBar.ButtonData.APPLY;
            dialog.close();
        } else {
            throw new IllegalStateException("Button should be disabled if validation failed!");
        }
    }

    public ButtonBar.ButtonData getResult() {
        return result;
    }
}
