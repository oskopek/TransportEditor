/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.planning.domain.VariableDomain;
import com.oskopek.transporteditor.view.EnterStringDialogPaneCreator;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller for choosing a course out of several choices.
 */
public class VariableDomainController extends AbstractController {

    private Dialog<ButtonType> dialog;

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
