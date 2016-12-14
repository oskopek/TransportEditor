package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.model.DefaultPlanningSession;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.persistence.*;
import com.oskopek.transporteditor.validation.VALValidator;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.ExecutableParametersCreator;
import com.oskopek.transporteditor.view.SaveDiscardDialogPaneCreator;
import com.oskopek.transporteditor.view.VariableDomainCreator;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles all action in the menu bar of the main app.
 */
@Singleton
public class RootLayoutController extends AbstractController {

    @Inject
    private ExecutableParametersCreator executableParametersCreator;

    @Inject
    private VariableDomainCreator variableDomainCreator;

    @Inject
    private transient Logger logger;

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
    private JavaFxOpenedTextObjectHandler<Plan> planFileHandler;

    @FXML
    private void initialize() {
        eventBus.register(this);

        planningSessionFileHandler = new JavaFxOpenedTextObjectHandler<DefaultPlanningSession>(application, messages,
                creator)
                .bind(sessionMenu, sessionNewMenuItem, sessionLoadMenuItem, sessionSaveMenuItem, sessionSaveAsMenuItem,
                        null).useXml();
        domainFileHandler = new JavaFxOpenedTextObjectHandler<VariableDomain>(application, messages, creator)
                .bind(domainMenu, domainNewMenuItem, domainLoadMenuItem, domainSaveMenuItem, domainSaveAsMenuItem,
                        planningSessionFileHandler).usePddl();
        problemFileHandler = new JavaFxOpenedTextObjectHandler<DefaultProblem>(application, messages, creator)
                .bind(problemMenu, problemNewMenuItem, problemLoadMenuItem, problemSaveMenuItem, problemSaveAsMenuItem,
                        domainFileHandler).usePddl();
        planFileHandler = new JavaFxOpenedTextObjectHandler<Plan>(application, messages, creator)
                .bind(planMenu, planNewMenuItem, planLoadMenuItem, planSaveMenuItem, planSaveAsMenuItem,
                        problemFileHandler).useVal();

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
        eventBus.post(new GraphUpdatedEvent());
    }

    @FXML
    private void handleSessionLoad() {
        DefaultPlanningSessionIO io = new DefaultPlanningSessionIO();
        planningSessionFileHandler.loadWithDefaultFileChooser(messages.getString("load.planningSession"), io, io);

        PlanningSession session = application.getPlanningSession();
        if (session != null) {
            Domain domain = session.getDomain();
            if (domain != null) {
                domainFileHandler.setObject((VariableDomain) domain); // TODO: casting hack
                session.domainProperty().bind(domainFileHandler.objectProperty());
                Problem problem = session.getProblem();
                if (problem != null) {
                    problemFileHandler.setObject((DefaultProblem) problem); // TODO: casting hack
                    session.problemProperty().bind(problemFileHandler.objectProperty());
                    Plan plan = session.getPlan();
                    if (plan != null) {
                        planFileHandler.setObject(plan);
                        session.planProperty().bind(planFileHandler.objectProperty());
                    }
                }
            }
        }
        eventBus.post(new GraphUpdatedEvent());
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
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot set planner, because no planning session is loaded.");
        }
        ExecutableWithParameters existing = null;
        try {
            existing = application.getPlanningSession().getPlanner().getExecutableWithParameters();
        } catch (NullPointerException e) {
            logger.trace("Got NPE while getting executable params for planner.", e);
            // best effort, ignore exception
        }
        ExecutableWithParameters executableWithParameters = executableParametersCreator
                .createExecutableWithParameters(2, messages.getString("planner.excreator.executable"),
                        messages.getString("planner.excreator.parameters"),
                        messages.getString("planner.excreator.note"), existing);
        if (executableWithParameters != null) {
            application.getPlanningSession().setPlanner(new ExternalPlanner(executableWithParameters));
        }
    }

    @FXML
    private void handleFileSetValidator() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot set validator, because no planning session is loaded.");
        }
        ExecutableWithParameters existing = null;
        try {
            existing = application.getPlanningSession().getValidator().getExecutableWithParameters();
        } catch (NullPointerException e) {
            logger.trace("Got NPE while getting executable params for validator.", e);
            // best effort, ignore exception
        }
        ExecutableWithParameters executableWithParameters = executableParametersCreator
                .createExecutableWithParameters(3, messages.getString("validator.excreator.executable"),
                        messages.getString("validator.excreator.parameters"),
                        messages.getString("validator.excreator.note"), existing);
        if (executableWithParameters != null) {
            application.getPlanningSession().setValidator(new VALValidator(executableWithParameters));
        }
    }

    @FXML
    private void handleDomainNew() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot create new domain, because no planning session is loaded.");
        }
        VariableDomain domain = variableDomainCreator.createDomainInDialog();
        if (domain != null) {
            VariableDomainIO guesser = new VariableDomainIO();
            domainFileHandler.newObject(domain, guesser, guesser);
            application.getPlanningSession().domainProperty().bind(domainFileHandler.objectProperty());
        }
        eventBus.post(new GraphUpdatedEvent());
    }

    @FXML
    private void handleDomainLoad() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot load domain, because no planning session is loaded.");
        }
        VariableDomainIO guesser = new VariableDomainIO();
        domainFileHandler.loadWithDefaultFileChooser(messages.getString("load.domain"), guesser, guesser);
        application.getPlanningSession().domainProperty().bind(domainFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
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
        RoadGraph graph = new RoadGraph("graph");
        graph.addLocation(new Location("Loc0", 0, 0));
        problemFileHandler.newObject(
                new DefaultProblem("problem" + new Date().toString(), graph, new HashMap<>(),
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
        problemFileHandler.loadWithDefaultFileChooser(messages.getString("load.problem"), io, io);
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
        eventBus.post(new PlanningFinishedEvent());
    }

    @FXML
    private void handlePlanLoad() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot load plan, because no problem is loaded.");
        }
        Domain domain = application.getPlanningSession().getDomain();
        Problem problem = application.getPlanningSession().getProblem();
        if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
            TemporalPlanIO io = new TemporalPlanIO(domain, problem);
            planFileHandler.loadWithDefaultFileChooser(messages.getString("load.plan"), io, io);
        } else {
            SequentialPlanIO io = new SequentialPlanIO(domain, problem);
            planFileHandler.loadWithDefaultFileChooser(messages.getString("load.plan"), io, io);
        }
        application.getPlanningSession().planProperty().bind(planFileHandler.objectProperty());
        eventBus.post(new PlanningFinishedEvent());
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
