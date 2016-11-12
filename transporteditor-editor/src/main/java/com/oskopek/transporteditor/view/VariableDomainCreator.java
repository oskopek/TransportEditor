package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.controller.VariableDomainController;
import com.oskopek.transporteditor.model.domain.VariableDomain;
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
import java.util.ResourceBundle;

/**
 * Dialog for entering a String. Creates a DialogPane encapsulated in it's controller.
 */
@Singleton
public class VariableDomainCreator {

    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;

    @Inject
    private ResourceBundle messages;

    /**
     * Create the dialog for creating a domain.
     *
     * @return the variable domain created from the user's settings
     */
    public VariableDomain createDomainInDialog() {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        DialogPane dialogPane = null;
        try (InputStream is = getClass().getResourceAsStream("VariableDomainCreatorPane.fxml")) {
            dialogPane = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(), e);
        }
        dialogPane.setHeaderText(messages.getString("domaincreator.title"));
        VariableDomainController variableDomainController = fxmlLoader.getController();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setDialogPane(dialogPane);
        variableDomainController.setDialog(dialog);
        dialog.setTitle("TransportEditor");
        dialog.showAndWait();
        return variableDomainController.getChosenDomain();
    }
}
