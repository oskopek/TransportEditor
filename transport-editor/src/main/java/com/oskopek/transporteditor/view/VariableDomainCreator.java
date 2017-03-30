package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.controller.VariableDomainController;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

/**
 * Dialog for creating a {@link VariableDomain}. Creates a DialogPane, shows it and waits.
 * Uses {@link com.oskopek.transporteditor.persistence.VariableDomainBuilder} to back itself.
 */
@Singleton
public class VariableDomainCreator {

    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;

    @Inject
    private ResourceBundle messages;

    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;

    /**
     * Create the dialog for creating a domain.
     *
     * @return the variable domain created from the user's choices
     */
    public VariableDomain createDomainInDialog() {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        BorderPane dialogPane = null;
        try (InputStream is = getClass().getResourceAsStream("VariableDomainCreatorPane.fxml")) {
            dialogPane = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(),
                    a -> application.centerInPrimaryStage(a, -200, -50), e);
        }
        dialogPane.getStylesheets().add(getClass().getResource("validation.css").toExternalForm());
        VariableDomainController variableDomainController = fxmlLoader.getController();

        Stage stage = new Stage();
        variableDomainController.setHeaderText(messages.getString("domaincreator.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(dialogPane));
        stage.setOnShown(e -> variableDomainController.getNameField().requestFocus());
        variableDomainController.setDialog(stage);
        stage.setTitle("TransportEditor");
        application.centerInPrimaryStage(stage, -200, -250);
        stage.showAndWait();
        return variableDomainController.getChosenDomain();
    }
}
