package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.view.ExecutableParametersCreator;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for choosing a course out of several choices.
 */
public class LogProgressController extends AbstractController {

    private Stage dialog;

    @FXML
    private Label headerText;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextArea logArea;

    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    private ButtonBar.ButtonData result;

    private Runnable cancellator;

    @FXML
    private void initialize() {
        logArea.setStyle("-fx-border-color: transparent");

        ButtonBar.setButtonData(okButton, ButtonBar.ButtonData.OK_DONE);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    public void setProgressConditions(ObservableValue<Boolean> successfullyFinished,
            ObservableValue<Boolean> cancelAvailable, ObservableValue<Boolean> inProgress, Runnable cancellator) {
        this.cancellator = cancellator;
        okButton.disableProperty().bind(BooleanBinding.booleanExpression(successfullyFinished).not());
        cancelButton.disableProperty().bind(BooleanBinding.booleanExpression(cancelAvailable).not());
        inProgress.addListener((observable, oldValue, newInProgress) -> {
            if (!newInProgress) {
                progressBar.setProgress(1.0);
                progressBar.setDisable(true);
            }
        });
    }

    public synchronized void appendLog(String logMessage) {
        Platform.runLater(() -> logArea.appendText(logMessage));
    }

    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    @FXML
    private void handleCancelButton() {
        if (!cancelButton.isDisabled()) {
            if (cancellator != null) {
                appendLog("\nCancelling process...\n");
                cancellator.run();
            }
            result = ButtonBar.ButtonData.CANCEL_CLOSE;
            dialog.close();
        } else {
            throw new IllegalStateException("Cancel button should have been disabled failed!");
        }
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
            throw new IllegalStateException("Ok button should have been disabled failed!");
        }
    }

    public ButtonBar.ButtonData getResult() {
        return result;
    }
}
