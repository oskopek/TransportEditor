package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.controller.LogProgressController;
import com.oskopek.transporteditor.view.executables.LogListener;
import com.oskopek.transporteditor.view.executables.LogStreamable;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
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
 * Dialog for showing log progress.
 */
@Singleton
public class LogProgressCreator {

    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;

    @Inject
    private ResourceBundle messages;

    /**
     * Create the dialog for showing log messages and progress.
     *
     * @return true iff the process completed successfully
     */
    public boolean createLogProgressDialog(LogStreamable logStreamable, ObservableValue<Boolean> successfullyFinished,
            ObservableValue<Boolean> cancelAvailable, ObservableValue<Boolean> inProgress, Runnable cancellator) {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        BorderPane dialogPane = null;
        try (InputStream is = getClass().getResourceAsStream("LogProgressViewer.fxml")) {
            dialogPane = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(), e);
        }
        LogProgressController logProgressController = fxmlLoader.getController();

        Stage stage = new Stage();
        logProgressController.setHeaderText(messages.getString("logprogress.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(dialogPane));
        logProgressController.setDialog(stage);
        LogListener logListener = logProgressController::appendLog;
        logStreamable.subscribe(logListener);
        logProgressController.setProgressConditions(successfullyFinished, cancelAvailable, inProgress, cancellator);
        stage.setTitle("TransportEditor");
        stage.showAndWait();
        logStreamable.unsubscribe(logListener);
        return ButtonBar.ButtonData.OK_DONE.equals(logProgressController.getResult());
    }
}
