package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.event.DisableShowStepEvent;
import com.oskopek.transporteditor.model.DefaultPlanningSession;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.persistence.*;
import com.oskopek.transporteditor.validation.SequentialPlanValidator;
import com.oskopek.transporteditor.validation.ValValidator;
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
import javaslang.Tuple;
import javaslang.collection.Stream;
import javaslang.control.Try;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles all action in the menu bar of the main app.
 */
@Singleton
public class RootLayoutController extends AbstractController {

    private final VariableDomainIO domainGuesser = new VariableDomainIO();
    @Inject
    private ExecutableParametersCreator executableParametersCreator;
    @Inject
    private VariableDomainCreator variableDomainCreator;
    @Inject
    private Logger logger;
    @Inject
    private SaveDiscardDialogPaneCreator creator;
    @FXML
    private Menu fileSetPlannerMenu;
    @FXML
    private Menu fileSetValidatorMenu;
    @FXML
    private Menu sessionMenu;
    @FXML
    private MenuItem sessionNewMenuItem;
    @FXML
    private MenuItem sessionLoadMenuItem;
    @FXML
    private MenuItem sessionSaveMenuItem;
    @FXML
    private MenuItem sessionSaveAsMenuItem;
    @FXML
    private Menu domainMenu;
    @FXML
    private MenuItem domainNewMenuItem;
    @FXML
    private MenuItem domainLoadMenuItem;
    @FXML
    private MenuItem domainSaveMenuItem;
    @FXML
    private MenuItem domainSaveAsMenuItem;
    @FXML
    private Menu problemMenu;
    @FXML
    private MenuItem problemNewMenuItem;
    @FXML
    private MenuItem problemLoadMenuItem;
    @FXML
    private MenuItem problemSaveMenuItem;
    @FXML
    private MenuItem problemSaveAsMenuItem;
    @FXML
    private Menu planMenu;
    @FXML
    private MenuItem planNewMenuItem;
    @FXML
    private MenuItem planLoadMenuItem;
    @FXML
    private MenuItem planSaveMenuItem;
    @FXML
    private MenuItem planSaveAsMenuItem;
    private JavaFxOpenedTextObjectHandler<Problem> problemFileHandler;
    private JavaFxOpenedTextObjectHandler<PlanningSession> planningSessionFileHandler;
    private JavaFxOpenedTextObjectHandler<Domain> domainFileHandler;
    private JavaFxOpenedTextObjectHandler<Plan> planFileHandler;

    /**
     * Util method for determining the correct plan IO class to use for the given domain and problem
     * and instantiating it.
     *
     * @param domain the domain
     * @param problem the problem
     * @return the instantiated plan IO
     */
    private static DataIO<Plan> createCorrectPlanIO(Domain domain, Problem problem) {
        if (domain.getPddlLabels().contains(PddlLabel.Temporal)) {
            return new TemporalPlanIO(domain, problem);
        } else {
            return new SequentialPlanIO(domain, problem);
        }
    }

    /**
     * JavaFX initializer method. Registers with the event bus. Initializes button disabling
     * and other validation. Initializes all IO handlers.
     */
    @FXML
    private void initialize() {
        eventBus.register(this);

        planningSessionFileHandler = new JavaFxOpenedTextObjectHandler<PlanningSession>(application, messages,
                creator)
                .bind(sessionMenu, sessionNewMenuItem, sessionLoadMenuItem, sessionSaveMenuItem, sessionSaveAsMenuItem,
                        null).useXml();
        domainFileHandler = new JavaFxOpenedTextObjectHandler<Domain>(application, messages, creator)
                .bind(domainMenu, domainNewMenuItem, domainLoadMenuItem, domainSaveMenuItem, domainSaveAsMenuItem,
                        planningSessionFileHandler).usePddl();
        problemFileHandler = new JavaFxOpenedTextObjectHandler<Problem>(application, messages, creator)
                .bind(problemMenu, problemNewMenuItem, problemLoadMenuItem, problemSaveMenuItem, problemSaveAsMenuItem,
                        domainFileHandler).usePddl();
        planFileHandler = new JavaFxOpenedTextObjectHandler<Plan>(application, messages, creator)
                .bind(planMenu, planNewMenuItem, planLoadMenuItem, planSaveMenuItem, planSaveAsMenuItem,
                        problemFileHandler).useVal();

        planFileHandler.objectProperty().addListener(l -> eventBus.post(new DisableShowStepEvent()));

        application.planningSessionProperty().bindBidirectional(planningSessionFileHandler.objectProperty());
        fileSetPlannerMenu.disableProperty().bind(application.planningSessionProperty().isNull());
        fileSetValidatorMenu.disableProperty().bind(application.planningSessionProperty().isNull());

        populatePlanners();
    }

    /**
     * Populate the {@link #fileSetPlannerMenu} using all non-abstract {@link Planner}s with a default empty constructor
     * from the {@link com.oskopek.transporteditor} package.
     * Uses {@link Reflections} internally.
     */
    private void populatePlanners() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClass(Planner.class))
                .setScanners(new SubTypesScanner(false))
                .filterInputsBy(s -> s != null && s.startsWith("com.oskopek.transporteditor.") && s.endsWith(".class"))
        );
        Stream.ofAll(reflections.getSubTypesOf(Planner.class))
                .filter(type -> !Modifier.isAbstract(type.getModifiers()))
                .map(type -> Tuple.of(type, Try.of(type::newInstance).toJavaOptional()))
                .map(tuple -> tuple.map((type, instance) -> Tuple.of(type.getSimpleName(), instance)))
                .filter(tuple -> tuple._2.isPresent())
                .map(tuple -> Tuple.of(tuple._1, tuple._2.get()))
                .filter(tuple -> tuple._2.isAvailable())
                .forEach(tuple -> addPlanner(tuple._1, tuple._2));
    }

    /**
     * Adds a planner to the {@link #fileSetPlannerMenu}.
     *
     * @param label the label to display in the menu item
     * @param planner the planner to set to the {@link PlanningSession} when clicked
     */
    private void addPlanner(String label, Planner planner) {
        logger.info("Adding planner \"{}\"", label);
        MenuItem item = new MenuItem(label);
        item.setOnAction(event -> {
            if (application.getPlanningSession() == null) {
                throw new IllegalStateException("Cannot set planner, because no planning session is loaded.");
            }
            application.getPlanningSession().setPlanner(planner);
        });
        fileSetPlannerMenu.getItems().add(item);
    }

    /**
     * Menu item: Session->Quit.
     * Exit the main app.
     */
    @FXML
    private void handleFileQuit() {
        Platform.exit();
    }

    /**
     * Menu item: Session->New.
     * Creates a new session.
     */
    @FXML
    private void handleSessionNew() {
        DefaultPlanningSessionIO io = new DefaultPlanningSessionIO();
        boolean overwritten = planningSessionFileHandler.newObject(new DefaultPlanningSession(), io, io);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Menu item: Session->Load.
     * Loads a session from a file.
     */
    @FXML
    private void handleSessionLoad() {
        DefaultPlanningSessionIO io = new DefaultPlanningSessionIO();
        boolean overwritten = planningSessionFileHandler.loadWithDefaultFileChooser(
                messages.getString("load.planningSession"), io, io);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        PlanningSession session = application.getPlanningSession();
        if (session != null) {
            Domain domain = session.getDomain();
            if (domain != null) {
                domainFileHandler.setObject(domain);
                domainFileHandler.setIO(new VariableDomainIO());
                session.domainProperty().bindBidirectional(domainFileHandler.objectProperty());
                Problem problem = session.getProblem();
                if (problem != null) {
                    if (application.getPlanningSession().getDomain() == null) {
                        throw new IllegalStateException(
                                "Cannot load problem from session, because no domain is loaded.");
                    }
                    problemFileHandler.setObject(problem);
                    problemFileHandler.setIO(new DefaultProblemIO(application.getPlanningSession().getDomain()));
                    session.problemProperty().bindBidirectional(problemFileHandler.objectProperty());
                    Plan plan = session.getPlan();
                    if (plan != null) {
                        if (application.getPlanningSession().getProblem() == null) {
                            throw new IllegalStateException(
                                    "Cannot load plan from session, because no problem is loaded.");
                        }
                        planFileHandler.setObject(plan);
                        planFileHandler.setIO(createCorrectPlanIO(application.getPlanningSession().getDomain(),
                                application.getPlanningSession().getProblem()));
                        session.planProperty().bindBidirectional(planFileHandler.objectProperty());
                    }
                }
            }
        }
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Menu item: Session->Save.
     * Save the session to the predetermined path.
     */
    @FXML
    private void handleSessionSave() {
        planningSessionFileHandler.save();
    }

    /**
     * Menu item: Session->Save As.
     * Saves the session to a chosen file.
     */
    @FXML
    private void handleSessionSaveAs() {
        planningSessionFileHandler.saveAsWithDefaultFileChooser();
    }

    /**
     * Menu item: Session->Set Planner->External Planner.
     * Set an external planner using a {@link ExecutableParametersCreator}'s dialog.
     */
    @FXML
    private void handleFileSetExternalPlanner() {
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

    /**
     * Menu item: Session->Set Validator->External validator.
     * Loads a custom executable validator using a {@link ExecutableParametersCreator}'s dialog.
     */
    @FXML
    private void handleFileSetExternalValidator() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot set custom validator, because no planning session is loaded.");
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
            application.getPlanningSession().setValidator(new ValValidator(executableWithParameters));
        }
    }

    /**
     * Menu item: Session->Set Validator->Sequential.
     * Loads the simple sequential validator.
     */
    @FXML
    private void handleFileSetSequentialValidator() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot set sequential validator, because no planning session is loaded.");
        }
        application.getPlanningSession().setValidator(new SequentialPlanValidator());
    }

    /**
     * Menu item: Domain->New.
     * Creates a new domain.
     */
    @FXML
    private void handleDomainNew() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot create new domain, because no planning session is loaded.");
        }
        VariableDomain domain = variableDomainCreator.createDomainInDialog();
        if (domain == null) {
            logger.debug("Not overwritten, returning");
            return;
        }

        VariableDomainIO guesser = new VariableDomainIO();

        boolean overwritten = domainFileHandler.newObject(domain, guesser, guesser);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        application.getPlanningSession().domainProperty().bindBidirectional(domainFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Menu item: Domain->Load.
     * Loads a domain from a chosen path.
     */
    @FXML
    private void handleDomainLoad() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot load domain, because no planning session is loaded.");
        }

        boolean overwritten = domainFileHandler
                .loadWithDefaultFileChooser(messages.getString("load.domain"), domainGuesser, domainGuesser);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        application.getPlanningSession().domainProperty().bindBidirectional(domainFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Menu item: Domain->Save.
     * Saves the domain to a predetermined file.
     */
    @FXML
    private void handleDomainSave() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot save domain, because no planning session is loaded.");
        }
        domainFileHandler.save();
    }

    /**
     * Menu item: Domain->Save As.
     * Saves the domain to a chosen file.
     */
    @FXML
    private void handleDomainSaveAs() {
        if (application.getPlanningSession() == null) {
            throw new IllegalStateException("Cannot save domain as, because no planning session is loaded.");
        }
        domainFileHandler.saveAsWithDefaultFileChooser();
    }

    /**
     * Menu item: Problem->New.
     * Creates a new problem.
     */
    @FXML
    private void handleProblemNew() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot create new problem, because no domain is loaded.");
        }
        Domain domain = application.getPlanningSession().getDomain();
        DefaultProblemIO io = new DefaultProblemIO(domain);
        RoadGraph graph = new RoadGraph("graph");
        Location location = new Location("loc0", 0, 0, null);
        if (domain.getPddlLabels().contains(PddlLabel.Fuel)) {
            location = location.updateHasPetrolStation(false);
        }
        graph.addLocation(location);

        boolean overwritten = problemFileHandler.newObject(new DefaultProblem("problem" + new Date().getTime(),
                graph, new HashMap<>(), new HashMap<>()), io, io);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        application.getPlanningSession().problemProperty().bindBidirectional(problemFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Menu item: Problem->Load.
     * Loads the problem from a chosen file.
     */
    @FXML
    private void handleProblemLoad() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot load problem, because no domain is loaded.");
        }
        DefaultProblemIO io = new DefaultProblemIO(application.getPlanningSession().getDomain());

        boolean overwritten = problemFileHandler.loadWithDefaultFileChooser(
                messages.getString("load.problem"), io, io);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        application.getPlanningSession().problemProperty().bindBidirectional(problemFileHandler.objectProperty());
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Menu item: Problem->Save.
     * Saves the problem to the predetermined file.
     */
    @FXML
    private void handleProblemSave() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot save problem, because no domain is loaded.");
        }
        problemFileHandler.save();
    }

    /**
     * Menu item: Problem->Save As.
     * Saves the problem to a chosen file.
     */
    @FXML
    private void handleProblemSaveAs() {
        if (application.getPlanningSession().getDomain() == null) {
            throw new IllegalStateException("Cannot save problem as, because no domain is loaded.");
        }
        problemFileHandler.saveAsWithDefaultFileChooser();
    }

    /**
     * Menu item: Plan->New.
     * Creates a new plan.
     */
    @FXML
    private void handlePlanNew() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot create new plan, because no problem is loaded.");
        }
        SequentialPlanIO io = new SequentialPlanIO(application.getPlanningSession().getDomain(),
                application.getPlanningSession().getProblem());

        boolean overwritten = planFileHandler.newObject(new SequentialPlan(new ArrayList<>()), io, io);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        application.getPlanningSession().planProperty().bindBidirectional(planFileHandler.objectProperty());
        eventBus.post(new PlanningFinishedEvent());
    }

    /**
     * Menu item: Plan->Load.
     * Loads a plan from the chosen file.
     */
    @FXML
    private void handlePlanLoad() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot load plan, because no problem is loaded.");
        }
        Domain domain = application.getPlanningSession().getDomain();
        Problem problem = application.getPlanningSession().getProblem();
        DataIO<Plan> io = createCorrectPlanIO(domain, problem);

        boolean overwritten = planFileHandler.loadWithDefaultFileChooser(messages.getString("load.plan"), io, io);
        if (!overwritten) {
            logger.debug("Not overwritten, returning");
            return;
        }

        application.getPlanningSession().planProperty().bindBidirectional(planFileHandler.objectProperty());
        eventBus.post(new PlanningFinishedEvent());
    }

    /**
     * Menu item: Plan->Save.
     * Saves the plan to the predetermined file.
     */
    @FXML
    private void handlePlanSave() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot save plan, because no problem is loaded.");
        }
        planFileHandler.save();
    }

    /**
     * Menu item: Plan->Save As.
     * Saves the plan to the chosen file.
     */
    @FXML
    private void handlePlanSaveAs() {
        if (application.getPlanningSession().getProblem() == null) {
            throw new IllegalStateException("Cannot save plan as, because no problem is loaded.");
        }
        planFileHandler.saveAsWithDefaultFileChooser();
    }

    /**
     * Menu item: Help->Help.
     * Shows a how-to help dialog.
     */
    @FXML
    private void handleAboutHelp() {
        loadWebResource("root.manualResource", "root.help", "root.manualNotAvailableInYourLanguage");
    }

    /**
     * Loads a web resource (used for local HTML files) and displays it in a modal dialog window.
     * Blocking call.
     *
     * @param resourceName the resource key in the default localization bundle that has the correct localized filename
     * @param titleResourceName the resource key of the title string
     * @param errorResourceName the resource key of the error string (error while loading resource)
     */
    private void loadWebResource(String resourceName, String titleResourceName, String errorResourceName) {
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        String resourceHtml = readResourceToString(messages.getString(resourceName));
        if (resourceHtml == null) {
            AlertCreator.showAlert(Alert.AlertType.WARNING, messages.getString(errorResourceName),
                    a -> application.centerInPrimaryStage(a, -200, -50));
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
     * Menu item: Help->Shortcut quicktips.
     * Shows a short modal shortcut tip dialog.
     */
    @FXML
    private void handleAboutShortcuts() {
        loadWebResource("root.shortcutResource", "root.shortcutQuickTips",
                "root.resourceNotAvailableInYourLanguage");
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
