package com.oskopek.bp.editor.controller;

import com.oskopek.bp.editor.persistence.DataReader;
import com.oskopek.bp.editor.persistence.DataWriter;
import com.oskopek.bp.editor.view.BPEditorApplication;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;

/**
 * Created by skopeko on 14.4.16.
 */
public class RootPaneController { // TODO a lot of stuff left out

    @Inject
    private transient Logger logger;

    @Inject
    private BPEditorApplication application;

    private File openedFile;

    private DataReader reader = null;

    private DataWriter writer = null;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;

    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private Menu helpMenu;

    @FXML
    private MenuItem aboutMenuItem;


    /**
     * Menu item: File->New.
     * Creates a new model in the main app.
     * Doesn't save the currently opened one!
     */
    @FXML
    private void handleNew() {
        openedFile = null;
    }

    /**
     * Menu item: File->Open.
     * Opens an existing model into the main app.
     * Doesn't save the currently opened one!
     */
    @FXML
    private void handleOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON StudyPlan", ".json", ".JSON"));
        File chosen = chooser.showOpenDialog(application.getPrimaryStage());
        if (chosen == null) {
            return;
        }
        openFromFile(chosen);
    }

    /**
     * Menu item: File->Save.
     * Save the opened model from the main app.
     * If there is no opened file and the model is not null, calls {@link #handleSaveAs()}.
     */
    @FXML
    private void handleSave() {
//        if (openedFile == null && application.getStudyPlan() != null) {
//            handleSaveAs();
//            return;
//        }
        saveToFile(openedFile);
    }

    /**
     * Menu item: File->Save As.
     * Save the opened model from the main app into a new file.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON StudyPlan", ".json", ".JSON"));
        File chosen = chooser.showSaveDialog(application.getPrimaryStage());
        if (chosen == null) {
            return;
        }
        saveToFile(chosen);
    }

    /**
     * Menu item: File->Quit.
     * Exit the main app.
     * Doesn't save the currently opened model!
     */
    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    /**
     * Menu item: Help->About.
     * Shows a short modal about dialog.
     */
    @FXML
    private void handleAbout() {
        Dialog<Label> dialog = new Dialog<>();
//        dialog.setContentText(
//                "                               StudyGuide\n" + "    <https://github.com/oskopek/StudyGuide>\n"
//                        + AbstractFXMLPane.messages.getString("menu.author") + ": Ondrej Skopek <oskopek@matfyz.cz>");
//        dialog.setTitle(AbstractFXMLPane.messages.getString("root.about"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void saveToFile(File file) {
        if (file == null) {
            logger.debug("Cannot save to null file.");
            return;
        }
//        try {
//            writer.writeTo(application.getStudyPlan(), file.getAbsolutePath());
//        } catch (IOException e) {
//            AbstractFXMLPane.showAlert(Alert.AlertType.ERROR, "Failed to save study plan: " + e);
//            e.printStackTrace();
//        }
    }

    private void openFromFile(File file) {
        if (file == null) {
            logger.debug("Cannot open from null file.");
            return;
        }
//        try {
//            application.setStudyPlan(reader.readFrom(file.getAbsolutePath()));
//        } catch (IOException e) {
//            AbstractFXMLPane.showAlert(Alert.AlertType.ERROR, "Failed to open study plan: " + e);
//            e.printStackTrace();
//        }
//        if (application.getStudyPlan() != null) {
//            openedFile = file;
//        }
//        application.reinitialize();
    }


}
