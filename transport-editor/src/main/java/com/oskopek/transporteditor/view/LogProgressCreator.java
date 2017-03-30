package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.controller.LogProgressController;
import com.oskopek.transport.tools.executables.LogListener;
import com.oskopek.transport.tools.executables.LogStreamable;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;

    /**
     * Create the dialog for showing log messages and progress.
     *
     * @param logStreamable the streamable to get logs from
     * @param successfullyFinished true iff we finished and the result was a success
     * @param cancelAvailable true if cancelling is a possibility (should be false after end of process)
     * @param inProgress true iff we process is in progress
     * @param cancellator the callback to run when we want to cancel the running process
     * @return true iff the process completed successfully
     */
    public boolean createLogProgressDialog(LogStreamable logStreamable, ObservableValue<Boolean> successfullyFinished,
            ObservableValue<Boolean> cancelAvailable, ObservableValue<Boolean> inProgress, Runnable cancellator) {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        BorderPane dialogPane = null;
        try (InputStream is = getClass().getResourceAsStream("LogProgressViewer.fxml")) {
            dialogPane = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(),
                    a -> application.centerInPrimaryStage(a, -200, -50), e);
        }
        LogProgressController logProgressController = fxmlLoader.getController();

        Stage stage = new Stage();
        logProgressController.setHeaderText(messages.getString("logprogress.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(dialogPane));
        stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.ESCAPE.equals(event.getCode()) && !inProgress.getValue()) {
                stage.close();
            }
        });
        logProgressController.setDialog(stage);
        LogListener logListener = logProgressController::appendLog;
        logStreamable.subscribe(logListener);
        logProgressController.setProgressConditions(successfullyFinished, cancelAvailable, inProgress, cancellator);
        stage.setTitle("TransportEditor");
        application.centerInPrimaryStage(stage, -200, -250);
        stage.showAndWait();
        logStreamable.unsubscribe(logListener);
        return ButtonBar.ButtonData.OK_DONE.equals(logProgressController.getResult());
    }
}
