package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.view.ExecutableParametersCreator;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for a dialog showing log messages and progress while waiting for an action to complete.
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

    /**
     * JavaFX initializer method.
     */
    @FXML
    private void initialize() {
        logArea.setStyle("-fx-border-color: transparent");

        ButtonBar.setButtonData(okButton, ButtonBar.ButtonData.OK_DONE);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    /**
     * Set the observable predicates used to determine the progress.
     *
     * @param successfullyFinished {@code finished & completed succesfully}, we can "ok" the dialog
     * @param cancelAvailable {@code !finished & a cancellable} was provided
     * @param inProgress {@code !finished}
     * @param cancellator the operation to run when the user wants to cancel
     */
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

    /**
     * Append a log message to the display.
     *
     * @param logMessage the log message to append
     */
    public void appendLog(String logMessage) {
        Platform.runLater(() -> logArea.appendText(logMessage));
    }

    /**
     * Set the header text. Should be localized before setting.
     *
     * @param headerText the localized header text
     */
    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    /**
     * Handles the cancel button. Invokes the cancellator supplied.
     */
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

    /**
     * Handles the OK button press. Applies the dialog.
     */
    @FXML
    private void handleOkButton() {
        if (!okButton.isDisabled()) {
            result = ButtonBar.ButtonData.APPLY;
            dialog.close();
        } else {
            throw new IllegalStateException("Ok button should have been disabled failed!");
        }
    }

    /**
     * Get the resulting button that was pressed by the user.
     *
     * @return the resulting button
     */
    public ButtonBar.ButtonData getResult() {
        return result;
    }
}
