package com.oskopek.transport.controller;

import com.oskopek.transport.view.EnterStringDialogPaneCreator;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller for choosing a course out of several choices.
 */
public class SaveDiscardController extends AbstractController {

    private Dialog<ButtonType> dialog;

    /**
     * Handles submitting the dialog in case the presses enter into the found course table.
     *
     * @param event the generated event
     */
    @FXML
    private void handleOnKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            yesDialog();
        }
    }

    /**
     * Closes the dialog as if the "YES" button was clicked.
     */
    private void yesDialog() {
        dialog.resultProperty().setValue(ButtonType.YES);
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
}
