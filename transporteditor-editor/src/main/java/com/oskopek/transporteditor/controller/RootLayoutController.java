package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.DefaultPlanningSession;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.persistence.DefaultPlanningSessionIO;
import com.oskopek.transporteditor.persistence.DefaultProblemIO;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.VariableDomainIO;
import com.oskopek.transporteditor.view.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles all action in the menu bar of the main app.
 */
@Singleton
public class RootLayoutController extends AbstractController {

    @Inject
    private EnterStringDialogPaneCreator enterStringDialogPaneCreator;

    @Inject
    private VariableDomainCreator variableDomainCreator;

    @Inject
    private transient Logger logger;

    @Inject
    private transient ResourceBundle messages;

    @Inject
    private transient TransportEditorApplication application;

    @Inject
    private transient SaveDiscardDialogPaneCreator creator;

    private JavaFxOpenedTextObjectHandler<DefaultProblem> problemFileHandler;
    private JavaFxOpenedTextObjectHandler<DefaultPlanningSession> planningSessionFileHandler;
    private JavaFxOpenedTextObjectHandler<VariableDomain> domainFileHandler;
    private JavaFxOpenedTextObjectHandler<SequentialPlan> planFileHandler;

    @FXML
    private void initialize() {
        eventBus.register(this);

        problemFileHandler = new JavaFxOpenedTextObjectHandler<>(application, messages, creator);
        planningSessionFileHandler = new JavaFxOpenedTextObjectHandler<>(application, messages, creator);
        domainFileHandler = new JavaFxOpenedTextObjectHandler<>(application, messages, creator);
        planFileHandler = new JavaFxOpenedTextObjectHandler<>(application, messages, creator);

        application.planningSessionProperty().bind(planningSessionFileHandler.objectProperty());
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
        DefaultPlanningSessionIO io = new DefaultPlanningSessionIO();
        planningSessionFileHandler.newObject(new DefaultPlanningSession(), io, io);
    }

    @FXML
    private void handleSessionLoad() {
        DefaultPlanningSessionIO io = new DefaultPlanningSessionIO();
        planningSessionFileHandler.load(messages.getString("load.planningSession"), io, io);
    }

    @FXML
    private void handleSessionSave() {
        planningSessionFileHandler.save();
    }

    @FXML
    private void handleSessionSaveAs() {
        planningSessionFileHandler.saveAs();
    }

    @FXML
    private void handleFileSetPlanner() {
        PlanningSession session = planningSessionFileHandler.getObject();
        if (session == null) {
            throw new IllegalStateException("Cannot set planner on null session.");
        }
        Path path = Paths.get(JavaFxOpenedTextObjectHandler.buildDefaultFileChooser(
                messages.getString("planner.executable")).showOpenDialog(application.getPrimaryStage()).toString());
        session.setPlanner(new ExternalPlanner(path.toAbsolutePath() + " {0} {1}"));
    }

    @FXML
    private void handleDomainNew() {
        VariableDomain domain = variableDomainCreator.createDomainInDialog();
        VariableDomainIO guesser = new VariableDomainIO();
        domainFileHandler.newObject(domain, guesser, guesser);
    }

    @FXML
    private void handleDomainLoad() {
        VariableDomainIO guesser = new VariableDomainIO();
        domainFileHandler.load(messages.getString("load.domain"), guesser, guesser);
    }

    @FXML
    private void handleDomainSave() {
        domainFileHandler.save();
    }

    @FXML
    private void handleDomainSaveAs() {
        domainFileHandler.saveAs();
    }

    @FXML
    private void handleProblemNew() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot create new problem, because no domain is loaded.");
        }
        DefaultProblemIO io = new DefaultProblemIO(application.getPlanningSession().getDomain());
        problemFileHandler.newObject(
                new DefaultProblem("problem" + new Date().toString(), new RoadGraph("graph"), new HashMap<>(),
                        new HashMap<>()), io, io);
    }

    @FXML
    private void handleProblemLoad() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot load problem, because no domain is loaded.");
        }
        DefaultProblemIO io = new DefaultProblemIO(application.getPlanningSession().getDomain());
        problemFileHandler.load(messages.getString("load.problem"), io, io);
    } // TODO: find a way to bind the loaders to the model + eventbus.post(graph)

    @FXML
    private void handleProblemSave() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot save problem, because no domain is loaded.");
        }
        problemFileHandler.save();
    }

    @FXML
    private void handleProblemSaveAs() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot save problem as, because no domain is loaded.");
        }
        problemFileHandler.saveAs();
    }

    @FXML
    private void handlePlanNew() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot create new plan, because no problem is loaded.");
        }
        SequentialPlanIO io = new SequentialPlanIO(application.getPlanningSession().getDomain(),
                application.getPlanningSession().getProblem());
        planFileHandler.newObject(new SequentialPlan(new ArrayList<>()), io, io);
    }

    @FXML
    private void handlePlanLoad() { // TODO: Auto-disabling of these buttons
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot load plan, because no problem is loaded.");
        }
        SequentialPlanIO io = new SequentialPlanIO(application.getPlanningSession().getDomain(),
                application.getPlanningSession().getProblem());
        planFileHandler.load(messages.getString("load.plan"), io, io);
    }

    @FXML
    private void handlePlanSave() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot save plan, because no problem is loaded.");
        }
        planFileHandler.save();
    }

    @FXML
    private void handlePlanSaveAs() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot save plan as, because no problem is loaded.");
        }
        planFileHandler.saveAs();
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
}
