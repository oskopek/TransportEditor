package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.planning.PlanningSession;
import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.DefaultRoad;
import com.oskopek.transporteditor.planning.problem.Location;
import com.oskopek.transporteditor.planning.problem.Problem;
import com.oskopek.transporteditor.planning.problem.RoadGraph;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.EnterStringDialogPaneCreator;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles all action in the menu bar of the main app.
 */
@Singleton
public class RootLayoutController extends AbstractController {

    public final OpenedTextObjectHandler<Problem> problemFileHandler = new JavaFxOpenedTextObjectHandler<>();
    private final OpenedTextObjectHandler<PlanningSession> planningSessionFileHandler
            = new JavaFxOpenedTextObjectHandler<>();
    private final OpenedTextObjectHandler<Domain> domainFileHandler = new JavaFxOpenedTextObjectHandler<>();
    private final OpenedTextObjectHandler<Plan> planFileHandler = new JavaFxOpenedTextObjectHandler<>();

    @Inject
    private EnterStringDialogPaneCreator enterStringDialogPaneCreator;

    @Inject
    private transient Logger logger;

    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    /**
     * Menu item: File->Quit.
     * Exit the main app.
     * Doesn't save the currently opened model!
     */
    @FXML
    private void handleFileQuit() {
        System.exit(0);
    }

    @FXML
    private void handleSessionNew() {
        // TODO implement me
    }

    private File openFileWithDefaultFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML", "*.xml");
        FileChooser.ExtensionFilter allFileFilter = new FileChooser.ExtensionFilter("All Files", "*");
        chooser.getExtensionFilters().addAll(allFileFilter, xmlFilter);
        chooser.setSelectedExtensionFilter(xmlFilter);
        return chooser.showOpenDialog(application.getPrimaryStage());
    }

    @FXML
    private void handleSessionLoad() {
        // TODO implement handles + JavaFX wrapper + disabling
        File chosen = openFileWithDefaultFileChooser("Load Planning Session");
        if (chosen == null) {
            return;
        }
        //        openFromFile(chosen);
    }


    @FXML
    private void handleSessionSave() {

    }

    @FXML
    private void handleSessionSaveAs() {

    }

    @FXML
    private void handleFileSetPlanner() {

    }

    @FXML
    private void handleDomainNew() {

    }

    @FXML
    private void handleDomainLoad() {

    }

    @FXML
    private void handleDomainSave() {

    }

    @FXML
    private void handleDomainSaveAs() {

    }

    @FXML
    private void handleProblemNew() {

    }

    @FXML
    private void handleProblemLoad() {
        // TODO: replace me with real impl
        RoadGraph graph = new RoadGraph("test");
        int nodes = (int) (System.currentTimeMillis() % 10) + 5;
        Location first = new Location(0 + "", 0, 0);
        Location preLast;
        Location last = first;
        graph.addLocation(last);
        for (int i = 1; i < nodes; i++) {
            preLast = last;
            last = new Location(i + "", 0, 0);
            graph.addLocation(last);
            graph.addRoad(new DefaultRoad("r" + i, ActionCost.valueOf(i)), preLast, last);
        }
        graph.addRoad(new DefaultRoad("last", ActionCost.valueOf(100)), last, first);
        eventBus.post(graph);
    }

    @FXML
    private void handleProblemSave() {

    }

    @FXML
    private void handleProblemSaveAs() {

    }

    @FXML
    private void handlePlanNew() {

    }

    @FXML
    private void handlePlanLoad() {

    }

    @FXML
    private void handlePlanSave() {

    }

    @FXML
    private void handlePlanSaveAs() {

    }

    /**
     * Menu item: File->Save As.
     * Save the opened model from the main app into a new file.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON", ".json", ".JSON"));
        File chosen = chooser.showSaveDialog(application.getPrimaryStage());
        if (chosen == null) {
            return;
        }
        //        saveToFile(chosen);
    }

    /**
     * Menu item: Help->Help.
     * Shows a how-to help dialog.
     */
    @FXML
    private void handleAboutHelp() {
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        String manualHtml = readResourceToString(messages.getString("root.manualResource"));
        if (manualHtml == null) {
            AlertCreator.showAlert(Alert.AlertType.WARNING,
                    messages.getString("root.manualNotAvailableInYourLanguage"));
            return;
        }

        Pattern messagePattern = Pattern.compile("%([a-z.A-Z]+)");
        Matcher messageMatcher = messagePattern.matcher(manualHtml);
        StringBuffer replaceHtmlBuffer = new StringBuffer();
        while (messageMatcher.find()) {
            String found = messageMatcher.group(1);
            logger.trace("Replacing {} in manual", found);
            String replacement = null;
            try {
                replacement = messages.getString(found);
            } catch (MissingResourceException e) {
                logger.warn("Couldn't find resource \"{}\" in messages.", found);
            }
            messageMatcher.appendReplacement(replaceHtmlBuffer, replacement == null ? found : replacement);
        }
        messageMatcher.appendTail(replaceHtmlBuffer);
        manualHtml = replaceHtmlBuffer.toString();
        webView.getEngine().loadContent(manualHtml);

        Stage webViewDialogStage = new Stage(StageStyle.DECORATED);
        webViewDialogStage.initOwner(application.getPrimaryStage());
        webViewDialogStage.setResizable(true);
        webViewDialogStage.setTitle("TransportEditor - " + messages.getString("root.help"));
        webViewDialogStage.initModality(Modality.NONE);
        webViewDialogStage.setScene(new Scene(webView));
        webViewDialogStage.toFront();
        webViewDialogStage.showAndWait();
    }

    /**
     * Reads a String resource into a stream and returns it as String.
     *
     * @param resource the resource to load from classpath
     * @return null iff the input stream was null (resource not found)
     */
    private String readResourceToString(String resource) {
        InputStream is = getClass().getResourceAsStream(resource);
        if (is == null) {
            logger.warn("Couldn't find resource \"{}\"", resource);
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read resource to string: " + resource, e);
        }
    }

    /**
     * Menu item: Help->About.
     * Shows a short modal about dialog.
     */
    @FXML
    private void handleAboutAbout() {
        Dialog<Label> dialog = new Dialog<>();
        dialog.setContentText("                               TransportEditor\n"
                + "    <https://github.com/oskopek/TransportEditor>\n" + messages.getString("menu.author")
                + ": Ondrej Skopek <oskopek@matfyz.cz>");
        dialog.setTitle(messages.getString("root.about"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    //    /**
    //     * Saves the currently opened model to a file.
    //     * If an {@link IOException} occurs, opens a modal dialog window notifying the user and prints a stack trace.
    //     *
    //     * @param file if null, does nothing
    //     * @see TransportEditorApplication#getPlanningSession()
    //     */
    //    private void saveToFile(File file) {
    //        if (file == null) {
    //            logger.debug("Cannot save to null file.");
    //            return;
    //        }
    //        try {
    //            writer.writeTo(application.getPlanningSession(), file.getAbsolutePath());
    //        } catch (IOException e) {
    //            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("root.openFailed") + ": " + e);
    //            e.printStackTrace();
    //            return;
    //        }
    //        openedFile = file;
    //    }
    //
    //    /**
    //     * Loads a model from a file into the main app.
    //     * If an {@link IOException} occurs, opens a modal dialog window notifying the user and prints a stack trace.
    //     *
    //     * @param file if null, does nothing
    //     * @see TransportEditorApplication#setPlanningSession(PlanningSession)
    //     */
    //    private void openFromFile(File file) {
    //        if (file == null) {
    //            logger.error("Cannot open from null file.");
    //            return;
    //        }
    //        PlanningSession oldSession = application.getPlanningSession();
    //        // notify the user something will happen (erase semester boxes)
    //        application.setPlanningSession(new DefaultPlanningSession());
    //        Task<PlanningSession> loadFromFileTask = new Task<PlanningSession>() {
    //            @Override
    //            protected PlanningSession call() throws Exception {
    //                DataReader<PlanningSession> reader = new JsonPlanningSessionReaderWriter();
    //                return reader.readFrom(file.getAbsolutePath());
    //            }
    //        };
    //        loadFromFileTask.setOnFailed(event -> {
    //            Throwable e = event.getSource().getException();
    //            Platform.runLater(() -> application.setPlanningSession(oldSession));
    //            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("root.openFailed") + ":\n\n" + e);
    //            logger.error("Exception during loading session: {}", e);
    //            e.printStackTrace();
    //        });
    //        loadFromFileTask.setOnSucceeded(event -> {
    //            PlanningSession newSession = loadFromFileTask.getValue();
    //            Platform.runLater(() -> application.setPlanningSession(newSession));
    //            openedFile = file;
    //        });
    //        new Thread(loadFromFileTask).start();
    //    }
}
