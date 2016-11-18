package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.event.GraphUpdatedEvent;
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
import com.oskopek.transporteditor.validation.VALValidator;
import com.oskopek.transporteditor.view.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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

    @FXML
    private transient MenuItem fileSetPlannerMenuItem;

    @FXML
    private transient MenuItem fileSetValidatorMenuItem;

    @FXML
    private transient Menu sessionMenu;

    @FXML
    private transient MenuItem sessionNewMenuItem;

    @FXML
    private transient MenuItem sessionLoadMenuItem;

    @FXML
    private transient MenuItem sessionSaveMenuItem;

    @FXML
    private transient MenuItem sessionSaveAsMenuItem;

    @FXML
    private transient Menu domainMenu;

    @FXML
    private transient MenuItem domainNewMenuItem;

    @FXML
    private transient MenuItem domainLoadMenuItem;

    @FXML
    private transient MenuItem domainSaveMenuItem;

    @FXML
    private transient MenuItem domainSaveAsMenuItem;

    @FXML
    private transient Menu problemMenu;

    @FXML
    private transient MenuItem problemNewMenuItem;

    @FXML
    private transient MenuItem problemLoadMenuItem;

    @FXML
    private transient MenuItem problemSaveMenuItem;

    @FXML
    private transient MenuItem problemSaveAsMenuItem;

    @FXML
    private transient Menu planMenu;

    @FXML
    private transient MenuItem planNewMenuItem;

    @FXML
    private transient MenuItem planLoadMenuItem;

    @FXML
    private transient MenuItem planSaveMenuItem;

    @FXML
    private transient MenuItem planSaveAsMenuItem;

    private JavaFxOpenedTextObjectHandler<DefaultProblem> problemFileHandler;
    private JavaFxOpenedTextObjectHandler<DefaultPlanningSession> planningSessionFileHandler;
    private JavaFxOpenedTextObjectHandler<VariableDomain> domainFileHandler;
    private JavaFxOpenedTextObjectHandler<SequentialPlan> planFileHandler;

    @FXML
    private void initialize() {
        eventBus.register(this);

        planningSessionFileHandler = new JavaFxOpenedTextObjectHandler<DefaultPlanningSession>(application, messages,
                creator)
                .bind(sessionMenu, sessionNewMenuItem, sessionLoadMenuItem, sessionSaveMenuItem, sessionSaveAsMenuItem,
                        null);
        domainFileHandler = new JavaFxOpenedTextObjectHandler<VariableDomain>(application, messages, creator)
                .bind(domainMenu, domainNewMenuItem, domainLoadMenuItem, domainSaveMenuItem, domainSaveAsMenuItem,
                        planningSessionFileHandler);
        problemFileHandler = new JavaFxOpenedTextObjectHandler<DefaultProblem>(application, messages, creator)
                .bind(problemMenu, problemNewMenuItem, problemLoadMenuItem, problemSaveMenuItem, problemSaveAsMenuItem,
                        domainFileHandler);
        planFileHandler = new JavaFxOpenedTextObjectHandler<SequentialPlan>(application, messages, creator)
                .bind(planMenu, planNewMenuItem, planLoadMenuItem, planSaveMenuItem, planSaveAsMenuItem,
                        problemFileHandler);

        // TODO: Be careful with bindings and handlers to not create a memleak
        application.planningSessionProperty().bind(planningSessionFileHandler.objectProperty());
        fileSetPlannerMenuItem.disableProperty().bind(application.planningSessionProperty().isNull());
        fileSetValidatorMenuItem.disableProperty().bind(application.planningSessionProperty().isNull());
    }

    /**
     * Menu item: File->Quit.
     * Exit the main app.
     */
    @FXML
    private void handleFileQuit() {
        Platform.exit();
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
    private void handleFileSetValidator() {
        PlanningSession session = planningSessionFileHandler.getObject();
        if (session == null) {
            throw new IllegalStateException("Cannot set validator on null session.");
        }
        Path path = Paths.get(JavaFxOpenedTextObjectHandler.buildDefaultFileChooser(
                messages.getString("validator.executable")).showOpenDialog(application.getPrimaryStage()).toString());
        session.setValidator(new VALValidator(path.toAbsolutePath() + " {0} {1}"));
    }

    @FXML
    private void handleDomainNew() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot create new domain, because no planning session is loaded.");
        }
        VariableDomain domain = variableDomainCreator.createDomainInDialog();
        VariableDomainIO guesser = new VariableDomainIO();
        domainFileHandler.newObject(domain, guesser, guesser);
        application.getPlanningSession().domainProperty().bind(domainFileHandler.objectProperty());
    }

    @FXML
    private void handleDomainLoad() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot load domain, because no planning session is loaded.");
        }
        VariableDomainIO guesser = new VariableDomainIO();
        domainFileHandler.load(messages.getString("load.domain"), guesser, guesser);
        application.getPlanningSession().domainProperty().bind(domainFileHandler.objectProperty());
    }

    @FXML
    private void handleDomainSave() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot save domain, because no planning session is loaded.");
        }
        domainFileHandler.save();
    }

    @FXML
    private void handleDomainSaveAs() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot save domain as, because no planning session is loaded.");
        }
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
        application.getPlanningSession().problemProperty().bind(problemFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
    }

    @FXML
    private void handleProblemLoad() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot load problem, because no domain is loaded.");
        }
        DefaultProblemIO io = new DefaultProblemIO(application.getPlanningSession().getDomain());
        problemFileHandler.load(messages.getString("load.problem"), io, io);
        application.getPlanningSession().problemProperty().bind(problemFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
    }

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
        application.getPlanningSession().planProperty().bind(planFileHandler.objectProperty());
    }

    @FXML
    private void handlePlanLoad() { // TODO: Auto-disabling of these buttons
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot load plan, because no problem is loaded.");
        }
        SequentialPlanIO io = new SequentialPlanIO(application.getPlanningSession().getDomain(),
                application.getPlanningSession().getProblem());
        planFileHandler.load(messages.getString("load.plan"), io, io);
        application.getPlanningSession().planProperty().bind(planFileHandler.objectProperty());
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
        loadWebResource("root.manualResource", "root.help", "root.manualNotAvailableInYourLanguage");
    }

    private void loadWebResource(String resourceName, String titleResourceName, String errorResourceName) {
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        String resourceHtml = readResourceToString(messages.getString(resourceName));
        if (resourceHtml == null) {
            AlertCreator.showAlert(Alert.AlertType.WARNING,
                    messages.getString(errorResourceName));
            return;
        }

        Pattern messagePattern = Pattern.compile("%([a-z.A-Z]+)");
        Matcher messageMatcher = messagePattern.matcher(resourceHtml);
        StringBuffer replaceHtmlBuffer = new StringBuffer();
        while (messageMatcher.find()) {
            String found = messageMatcher.group(1);
            logger.trace("Replacing {} in resource", found);
            String replacement = null;
            try {
                replacement = messages.getString(found);
            } catch (MissingResourceException e) {
                logger.warn("Couldn't find resource \"{}\" in messages.", found);
            }
            messageMatcher.appendReplacement(replaceHtmlBuffer, replacement == null ? found : replacement);
        }
        messageMatcher.appendTail(replaceHtmlBuffer);
        resourceHtml = replaceHtmlBuffer.toString();

        Stage webViewDialogStage = new Stage(StageStyle.DECORATED);
        BooleanProperty clicked = new SimpleBooleanProperty(false);
        webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !clicked.get()) {
                logger.debug("Redirecting link to web browser: {}", newValue);
                clicked.setValue(true);
                webView.getEngine().getLoadWorker().cancel();
                application.getHostServices().showDocument(newValue);
                webViewDialogStage.close();
            }
        });
        webView.getEngine().loadContent(resourceHtml);

        webViewDialogStage.initOwner(application.getPrimaryStage());
        webViewDialogStage.setResizable(true);
        webViewDialogStage.setTitle("TransportEditor - " + messages.getString(titleResourceName));
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
        loadWebResource("root.aboutResource", "root.about", "root.resourceNotAvailableInYourLanguage");
    }
}
