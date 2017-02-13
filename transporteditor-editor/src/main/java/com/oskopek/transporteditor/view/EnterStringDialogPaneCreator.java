package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.controller.EnterStringController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

/**
 * Dialog for entering a String. Creates a DialogPane encapsulated in it's controller.
 */
@Singleton
public class EnterStringDialogPaneCreator {

    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;

    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;

    /**
     * Create the dialog for entering a String.
     *
     * @param prompt the string message to prompt the user with
     * @return the controller of the dialog window, enabling to display the dialog and read the selected result
     */
    public EnterStringController create(String prompt) {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        DialogPane dialogPane = null;
        try (InputStream is = getClass().getResourceAsStream("EnterStringDialogPane.fxml")) {
            dialogPane = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(),
                    a -> application.centerInPrimaryStage(a, -200, -50), e);
        }
        dialogPane.setHeaderText(prompt);
        EnterStringController enterStringController = fxmlLoader.getController();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setDialogPane(dialogPane);
        enterStringController.setDialog(dialog);
        dialog.setTitle("TransportEditor");
        application.centerInPrimaryStage(dialog, -100, -150);
        dialog.setOnShown(event -> enterStringController.getTextField().requestFocus());
        return enterStringController;
    }
}
