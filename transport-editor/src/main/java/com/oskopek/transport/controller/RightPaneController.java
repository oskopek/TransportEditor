package com.oskopek.transport.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.oskopek.transport.event.GraphUpdatedEvent;
import com.oskopek.transport.event.PlanningFinishedEvent;
import com.oskopek.transport.view.*;
import com.oskopek.transport.view.plan.SequentialPlanTable;
import com.oskopek.transport.event.DisableShowStepEvent;
import com.oskopek.transport.model.PlanningSession;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.state.PlanStateManager;
import com.oskopek.transport.model.state.TemporalPlanStateManager;
import com.oskopek.transport.validation.Validator;
import com.oskopek.transporteditor.view.*;
import com.oskopek.transport.view.plan.TemporalGanttChart;
import com.oskopek.transport.view.plan.TemporalPlanTable;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javaslang.control.Try;
import org.controlsfx.control.table.TableFilter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Controller for the right pane - contains plan views and the graph edit toolbox of buttons.
 */
@Singleton
public class RightPaneController extends AbstractController {

    private final BooleanProperty stepPreviewEnabled = new SimpleBooleanProperty(false);
    private ObjectProperty<PlanStateManager> planStateManager;
    @Inject
    private transient Logger logger;
    @Inject
    private LogProgressCreator logProgressCreator;
    @FXML
    private TabPane planTabPane;
    @FXML
    private Tab temporalPlanTab;
    @FXML
    private Tab sequentialPlanTab;
    @FXML
    private Tab ganttPlanTab;
    @FXML
    private ScrollPane sequentialPlanTabScrollPane;
    @FXML
    private ScrollPane temporalPlanTabScrollPane;
    @FXML
    private ScrollPane ganttPlanTabScrollPane;
    private TableFilter<TemporalPlanAction> actionTableFilter;
    private ListChangeListener<? super TemporalPlanAction> lastChangeListener;
    private ChangeListener<? super TemporalPlanAction> rowSelectionChangeListener;
    @FXML
    private Button planButton;
    @FXML
    private Button validateButton;
    @FXML
    private Button redrawButton;
    @FXML
    private Button addLocationButton;
    @FXML
    private Button addRoadButton;
    @FXML
    private Button addVehicleButton;
    @FXML
    private Button addPackageButton;
    @FXML
    private Button lockButton;
    @FXML
    private Button stepButton;
    @FXML
    private RadioButton startTimeButton;
    @FXML
    private RadioButton middleTimeButton;
    @FXML
    private RadioButton endTimeButton;
    private ToggleGroup actionTimeGroup = new ToggleGroup();
    @FXML
    private ToggleButton applyStartsButton;
    @FXML
    private HBox stepRow;
    @FXML
    private Spinner<Double> timeSpinner;
    @Inject
    private CenterPaneController centerPaneController;
    @Inject
    private EventBus eventBus;
    private InvalidationListener stepUpdated;
    private boolean thirdPartySelection = false;
    private boolean thirdPartySpinnerChange = false;

    /**
     * JavaFX initializer method. Registers with the event bus. Initializes button disabling
     * and other validation.
     */
    @FXML
    private void initialize() {
        planStateManager = new SimpleObjectProperty<>();
        eventBus.register(this);
        stepUpdated = l -> centerPaneController.getProblemSupplier().get().ifPresent(p ->
                p.getRoadGraph().redrawPackagesVehiclesFromPlanState(planStateManager.get().getCurrentPlanState()));

        stepRow.managedProperty().bind(stepPreviewEnabled);
        stepRow.visibleProperty().bind(stepRow.managedProperty());

        updateTimeSpinner();
        timeSpinner.valueProperty().addListener(l -> {
            Double value = timeSpinner.getValue();
            if (value == null) {
                return;
            }
            if (thirdPartySpinnerChange) {
                thirdPartySpinnerChange = false;
                return;
            }

            planStateManager.get().goToTime(value, applyStartsButton.isSelected());
            redrawState();
            updateTableSelection();
            if (applyStartsButton.isSelected()) {
                actionTimeGroup.selectToggle(startTimeButton);
            } else {
                actionTimeGroup.selectToggle(endTimeButton);
            }
        });

        rowSelectionChangeListener = (observable, oldValue, newValue) -> {
            if (!stepPreviewEnabled.get()) {
                return;
            }
            if (thirdPartySelection) {
                thirdPartySelection = false;
                return;
            }
            if (newValue != null) {
                applyStartsButton.setSelected(false);
                updateFromTimeButtons(newValue);
                redrawState();
                updateTimeSpinner();
            }
        };

        actionTimeGroup.getToggles().addAll(startTimeButton, middleTimeButton, endTimeButton);
        actionTimeGroup.selectToggle(endTimeButton);
        applyStartsButton.setSelected(false);

        initializeBindings();
    }

    /**
     * Initialize all bindings.
     */
    private void initializeBindings() {
        // Disable plan button condition
        InvalidableOrBooleanBinding domainBinding
                = new InvalidableOrBooleanBinding(application.planningSessionProperty().isNull())
                .or(new IsNullBinding(PlanningSession::domainProperty));
        InvalidableOrBooleanBinding problemBinding = domainBinding
                .or(new IsNullBinding(PlanningSession::problemProperty));
        InvalidableOrBooleanBinding disablePlanButtonBinding = problemBinding
                .or(new IsNullBinding(PlanningSession::plannerProperty));
        planButton.disableProperty().bind(disablePlanButtonBinding);

        // Disable lock button condition
        InvalidableOrBooleanBinding disableStepButtonBinding = problemBinding
                .or(new IsNullBinding(PlanningSession::planProperty));
        stepButton.disableProperty().bind(disableStepButtonBinding);

        // Disable validate button condition
        InvalidableOrBooleanBinding disableValidateButtonBinding = problemBinding
                .or(new IsNullBinding(PlanningSession::planProperty))
                .or(new IsNullBinding(PlanningSession::validatorProperty));
        validateButton.disableProperty().bind(disableValidateButtonBinding);

        // Disable lock button condition
        InvalidableOrBooleanBinding disableLockButtonBinding = problemBinding.or(new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return stepPreviewEnabled.get();
            }
        });
        lockButton.disableProperty().bind(disableLockButtonBinding);
        stepRow.visibleProperty().addListener(s -> disableLockButtonBinding.invalidate());

        // Disable addLocation button condition
        InvalidableOrBooleanBinding problemLockedBinding = problemBinding
                .or(new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return centerPaneController.isLocked();
                    }
                });
        centerPaneController.lockedProperty().addListener(e -> problemLockedBinding.invalidate());
        addLocationButton.disableProperty().bind(problemLockedBinding);

        // Disable redraw button condition
        InvalidableOrBooleanBinding disableRedrawButtonBinding = problemLockedBinding.copyWithoutListeners();
        redrawButton.disableProperty().bind(disableRedrawButtonBinding);

        // Disable graph changes (addVehicle) button condition
        InvalidableOrBooleanBinding disableAddVehicleButtonBinding = problemLockedBinding
                .or(new OptionalSelectionBinding<>(
                        () -> Optional.ofNullable(centerPaneController.getGraphSelectionHandler()),
                        r -> !r.doesSelectionDeterminePossibleNewVehicle()));
        addVehicleButton.disableProperty().bind(disableAddVehicleButtonBinding);

        // Disable graph changes (addRoad and addPackage) button condition
        InvalidableOrBooleanBinding disableAddRoadButtonBinding = problemLockedBinding
                .or(new OptionalSelectionBinding<>(
                        () -> Optional.ofNullable(centerPaneController.getGraphSelectionHandler()),
                        r -> !r.doesSelectionDeterminePossibleNewRoad()));
        addRoadButton.disableProperty().bind(disableAddRoadButtonBinding);
        addPackageButton.disableProperty().bind(disableAddRoadButtonBinding);

        centerPaneController.lockedProperty().addListener(e -> {
            problemLockedBinding.invalidate();
            disableAddVehicleButtonBinding.invalidate();
            disableAddRoadButtonBinding.invalidate();
            disableRedrawButtonBinding.invalidate();
        });

        // Update disable AddRoad and AddVehicle button
        InvalidationListener graphSelectionChangedListener = e -> {
            disableAddRoadButtonBinding.invalidate();
            disableAddVehicleButtonBinding.invalidate();
        };
        centerPaneController.graphSelectionHandlerProperty().addListener(graphSelectionChangedListener);
        centerPaneController.graphSelectionHandlerProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeListener(graphSelectionChangedListener);
            }
            if (newValue != null) {
                newValue.addListener(graphSelectionChangedListener);
            }
        });

        // Update disable planButton, validateButton, lockButton, graphChange and addRoad buttons on session change
        InvalidationListener invalidatePlanButtonBindingListener = s -> {
            disablePlanButtonBinding.invalidate();
            disableValidateButtonBinding.invalidate();
            disableRedrawButtonBinding.invalidate();
            disableLockButtonBinding.invalidate();
            disableAddVehicleButtonBinding.invalidate();
            disableAddRoadButtonBinding.invalidate();
            problemLockedBinding.invalidate();
            disableStepButtonBinding.invalidate();
        };
        application.planningSessionProperty().addListener(invalidatePlanButtonBindingListener);
        application.planningSessionProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeListener(invalidatePlanButtonBindingListener);
            }
            if (newValue != null) {
                newValue.addListener(invalidatePlanButtonBindingListener);
            }
        });
    }

    /**
     * Redraw plan views without selecting a row of the plan.
     *
     * @param event the event subscribed to
     */
    @Subscribe
    public void redrawPlans(GraphUpdatedEvent event) {
        redrawPlansInternal(null);
    }

    /**
     * Redraw plan views and select a row of the plan, if one was provided in the event.
     *
     * @param event the event subscribed to
     */
    @Subscribe
    public void redrawPlans(PlanningFinishedEvent event) {
        redrawPlansInternal(event.getSelectRow());
    }

    /**
     * Redraw the plan views and potentially select a row of the plan.
     * Applies filtering to the plan tables and synchronizes the Gantt view with it.
     *
     * @param selectedRow null = no selection, otherwise the index from 0 of the action to select in the plan
     */
    private void redrawPlansInternal(Integer selectedRow) {
        logger.debug("Caught planning finished event: redrawing plans.");
        Platform.runLater(() -> {
            Plan plan = null;
            try {
                plan = application.getPlanningSession().getPlan();
            } catch (NullPointerException e) {
                logger.trace("Tried to redraw plans, caught NPE along the way.", e);
            }

            if (actionTableFilter != null) {
                actionTableFilter.getFilteredList().removeListener(lastChangeListener);
                actionTableFilter.getTableView().getSelectionModel().selectedItemProperty()
                        .removeListener(rowSelectionChangeListener);
                lastChangeListener = null;
                actionTableFilter = null;
            }

            if (plan == null) {
                temporalPlanTabScrollPane.setContent(null);
                ganttPlanTabScrollPane.setContent(null);
                sequentialPlanTabScrollPane.setContent(null);

                sequentialPlanTab.setDisable(false);
                temporalPlanTab.setDisable(false);
                planTabPane.setDisable(true);
            } else {
                planTabPane.setDisable(false);
                boolean isDomainTemporal = application.getPlanningSession().getDomain().getPddlLabels()
                        .contains(PddlLabel.Temporal);
                sequentialPlanTab.setDisable(isDomainTemporal);
                temporalPlanTab.setDisable(!isDomainTemporal);

                if (isDomainTemporal) {
                    actionTableFilter = TemporalPlanTable.build(plan.getTemporalPlanActions(), (list) ->
                            application.getPlanningSession().setPlan(new TemporalPlan(list)));
                    temporalPlanTabScrollPane.setContent(actionTableFilter.getTableView());
                    sequentialPlanTabScrollPane.setContent(null);
                    planTabPane.getSelectionModel().select(temporalPlanTab);
                } else {
                    actionTableFilter = SequentialPlanTable.build(plan.getActions(), (list, index) -> {
                        application.getPlanningSession().setPlan(new SequentialPlan(list));
                        eventBus.post(new PlanningFinishedEvent(index));
                    });
                    sequentialPlanTabScrollPane.setContent(actionTableFilter.getTableView());
                    temporalPlanTabScrollPane.setContent(null);
                    planTabPane.getSelectionModel().select(sequentialPlanTab);
                }

                if (selectedRow != null) {
                    actionTableFilter.getTableView().getSelectionModel().select(selectedRow);
                }

                lastChangeListener = c -> ganttPlanTabScrollPane.setContent(TemporalGanttChart.build(c.getList()));
                actionTableFilter.getFilteredList().addListener(lastChangeListener);
                actionTableFilter.getTableView().getSelectionModel().selectedItemProperty()
                        .addListener(rowSelectionChangeListener);
                ganttPlanTabScrollPane.setContent(TemporalGanttChart.build(actionTableFilter.getFilteredList()));

            }
        });
    }

    /**
     * Handle the Plan button press. Starts planning and orchestrates UI events surrounding it.
     * Displays a waiting dialog and possible followup actions to the planning.
     */
    @FXML
    private void handlePlan() {
        logger.debug("Starting planning...");
        CompletionStage<Plan> planFuture = application.getPlanningSession().startPlanningAsync();
        Planner planner = application.getPlanningSession().getPlanner();
        BooleanProperty successful = new SimpleBooleanProperty(false);
        BooleanProperty completed = new SimpleBooleanProperty(false);
        planFuture.thenAcceptAsync(plan -> {
            logger.debug("Planning finished. Plan: {}", plan);
            Platform.runLater(() -> {
                successful.setValue(plan != null);
                completed.setValue(true);
                if (plan == null) {
                    AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("planning.failed") + ": "
                                    + messages.getString("planner.nullplan"),
                            a -> application.centerInPrimaryStage(a, -200, -50), ButtonType.OK);
                }
            });
        }).thenRunAsync(() -> {
            logger.trace("EventBus begin");
            Platform.runLater(() -> {
                logger.trace("InEventBus begin");
                eventBus.post(new PlanningFinishedEvent());
                logger.trace("InEventBus end");
            });
            logger.trace("EventBus end");
        }).exceptionally(
                throwable -> {
                    Platform.runLater(() -> completed.setValue(true));
                    logger.debug("Planning failed.", throwable);
                    AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("planning.failed") + ": "
                                    + throwable.getMessage(), a -> application.centerInPrimaryStage(a, -200, -50),
                            ButtonType.OK);
                    return null;
                });
        logger.trace("LogProgress begin");
        logProgressCreator.createLogProgressDialog(application.getPlanningSession().getPlanner(), successful,
                completed.not(), completed.not(), planner::cancel);
        logger.trace("LogProgress end");
    }

    /**
     * Handle the Validate button press. Starts validation and orchestrates UI events surrounding it.
     * Displays a waiting dialog and possible followup actions to the validation.
     */
    @FXML
    private void handleValidate() {
        logger.debug("Starting validation...");
        CompletionStage<Boolean> validationFuture = application.getPlanningSession().startValidationAsync();
        Validator validator = application.getPlanningSession().getValidator();
        BooleanProperty successful = new SimpleBooleanProperty(false);
        BooleanProperty completed = new SimpleBooleanProperty(false);
        validationFuture.thenAcceptAsync(isValid -> Platform.runLater(() -> {
            completed.setValue(true);
            if (isValid) {
                successful.setValue(true);
                AlertCreator.showAlert(Alert.AlertType.INFORMATION, messages.getString("validation.valid"),
                        a -> application.centerInPrimaryStage(a, -50, -50), ButtonType.CLOSE);
            } else {
                AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("validation.invalid"),
                        a -> application.centerInPrimaryStage(a, -50, -50), ButtonType.CLOSE);
            }
        })).exceptionally(throwable -> {
            Platform.runLater(() -> completed.setValue(true));
            AlertCreator.showAlert(Alert.AlertType.ERROR,
                    messages.getString("validation.failed") + ": " + throwable.getMessage(),
                    a -> application.centerInPrimaryStage(a, -150, -50), ButtonType.CLOSE);
            return null;
        }).toCompletableFuture();
        logProgressCreator.createLogProgressDialog(application.getPlanningSession().getValidator(), successful,
                completed.not(), completed.not(), validator::cancel);
    }

    /**
     * Handles the Redraw button being pressed. Posts a {@link GraphUpdatedEvent}.
     */
    @FXML
    private void handleRedraw() {
        logger.debug("Starting redraw...");
        eventBus.post(new GraphUpdatedEvent());
    }

    /**
     * Handles the +Location button being pressed. Adds a location to the graph and displays it in the graph.
     * Does not verify that we are in a good state to add a road, that should be done by button disabling
     * validation.
     */
    @FXML
    private void handleAddLocation() {
        RoadGraph graph = null;
        try {
            graph = application.getPlanningSession().getProblem().getRoadGraph();
        } catch (NullPointerException e) {
            logger.trace("Tried to get graph, caught NPE along the way.", e);
        }

        if (graph == null) {
            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("add.nograph"),
                    a -> application.centerInPrimaryStage(a, -200, -50), ButtonType.CLOSE);
        } else {
            String name = "loc" + graph.getNodeCount();
            while (graph.getLocation(name) != null) {
                name += "-1";
            }
            Node node;
            if (application.getPlanningSession().getDomain().getPddlLabels().contains(PddlLabel.Fuel)) {
                node = graph.addLocation(new Location(name, 0, 0, false));
            } else {
                node = graph.addLocation(new Location(name, 0, 0, null));
            }
            centerPaneController.getGraphSelectionHandler().selectOnly(node);
        }
    }

    /**
     * Handles the +Road button being pressed. Adds a road to the graph and displays it.
     * Does not verify that we are in a good state to add a road, that should be done by button disabling
     * validation.
     */
    @FXML
    private void handleAddRoad() {
        RoadGraph graph = null;
        try {
            graph = application.getPlanningSession().getProblem().getRoadGraph();
        } catch (NullPointerException e) {
            logger.trace("Tried to get graph, caught NPE along the way.", e);
        }

        if (graph == null) {
            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("add.nograph"),
                    a -> application.centerInPrimaryStage(a, -200, -50), ButtonType.CLOSE);
        } else {
            GraphSelectionHandler handler = centerPaneController.getGraphSelectionHandler();
            if (!handler.doesSelectionDeterminePossibleNewRoad()) {
                throw new IllegalStateException("Can't add new road when selection doesn't determine a road,"
                        + " button shouldn't be enabled.");
            }
            if (handler.doesSelectionDetermineExistingRoads()) {
                AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("add.road.exists"),
                        a -> application.centerInPrimaryStage(a, -200, -50), ButtonType.CLOSE);
                return;
            }

            String name = "road" + graph.getEdgeCount();
            while (graph.getRoad(name) != null) {
                name += "-1";
            }
            Location from = handler.getSelectedLocationList().get(0);
            Location to = handler.getSelectedLocationList().get(1);

            Edge edge;
            if (application.getPlanningSession().getDomain().getPddlLabels().contains(PddlLabel.Fuel)) {
                edge = graph.addRoad(new FuelRoad(name, ActionCost.valueOf(1)), from, to);
            } else {
                edge = graph.addRoad(new DefaultRoad(name, ActionCost.valueOf(1)), from, to);
            }

            Platform.runLater(() -> {
                Try.run(() -> Thread.sleep(100)).get(); // TODO: Hack - needs to happen later
                centerPaneController.getGraphSelectionHandler().selectOnly(edge);
            });
        }
    }

    /**
     * Handles the +Vehicle button being pressed. Adds a vehicle to the graph and displays it.
     * Does not verify that we are in a good state to add a vehicle, that should be done by button disabling
     * validation.
     */
    @FXML
    private void handleAddVehicle() {
        Problem problem = application.getPlanningSession().getProblem();
        String name = "truck-" + problem.getAllVehicles().size();
        while (problem.getVehicle(name) != null) {
            name += "-1";
        }
        GraphSelectionHandler handler = centerPaneController.getGraphSelectionHandler();
        Location at = handler.getSelectedLocationList().get(0);

        Vehicle vehicle;
        if (application.getPlanningSession().getDomain().getPddlLabels().contains(PddlLabel.Fuel)) {
            vehicle = new Vehicle(name, at, null, ActionCost.valueOf(0), ActionCost.valueOf(0), ActionCost.valueOf(0),
                    ActionCost.valueOf(0), true, new ArrayList<>());
        } else {
            vehicle = new Vehicle(name, at, null, ActionCost.valueOf(0), ActionCost.valueOf(0), true,
                    new ArrayList<>());
        }

        problem = problem.putVehicle(name, vehicle);
        application.getPlanningSession().setProblem(problem);
        centerPaneController.refreshGraphSelectionHandler();
        problem.getRoadGraph().redrawActionObjectSprites(problem);
        Platform.runLater(() -> {
            Try.run(() -> Thread.sleep(100)).get(); // TODO: Hack - needs to happen later
            centerPaneController.getGraphSelectionHandler().selectOnly(vehicle);
        });
    }

    /**
     * Handles the +Package button being pressed. Adds a package to the graph and displays it.
     * Does not verify that we are in a good state to add a package, that should be done by button disabling
     * validation.
     */
    @FXML
    private void handleAddPackage() {
        Problem problem = application.getPlanningSession().getProblem();
        String name = "package-" + (problem.getAllPackages().size() + 1);
        while (problem.getPackage(name) != null) {
            name += "-1";
        }
        GraphSelectionHandler handler = centerPaneController.getGraphSelectionHandler();
        Location at = handler.getSelectedLocationList().get(0);
        Location target = handler.getSelectedLocationList().get(1);
        Package pkg = new Package(name, at, target, ActionCost.valueOf(0));
        problem = problem.putPackage(name, pkg);
        application.getPlanningSession().setProblem(problem);
        centerPaneController.refreshGraphSelectionHandler();
        problem.getRoadGraph().redrawActionObjectSprites(problem);
        Platform.runLater(() -> {
            Try.run(() -> Thread.sleep(100)).get(); // TODO: Hack - needs to happen later
            centerPaneController.getGraphSelectionHandler().selectOnly(pkg);
        });
    }

    /**
     * Handles the Lock button press. Changes the buttons appearance and switches the lock property
     * that all the editing methods check before executing.
     */
    @FXML
    private void handleLockToggle() {
        if (centerPaneController.isLocked()) {
            unlock();
        } else {
            lock();
        }
    }

    /**
     * Lock the problem graph.
     */
    private void lock() {
        centerPaneController.setLocked(true);
        lockButton.setText(messages.getString("unlock"));
        lockButton.setStyle("-fx-text-fill: green;");
    }

    /**
     * Unlock the problem graph.
     */
    private void unlock() {
        centerPaneController.setLocked(false);
        lockButton.setText(messages.getString("lock"));
        lockButton.setStyle("-fx-text-fill: red;");
    }

    /**
     * Disables the step showing mode when the appropriate event is received.
     *
     * @param event the received event
     */
    @Subscribe
    public void disableShowStep(DisableShowStepEvent event) {
        if (stepPreviewEnabled.get()) {
            changeSetPreview(false);
        }
    }

    /**
     * Changes the step preview mode according to the passed parameter.
     *
     * @param newStepPreviewEnabled whether we should enable (true) or disable (false) the step preview mode
     */
    private void changeSetPreview(boolean newStepPreviewEnabled) {
        stepPreviewEnabled.set(newStepPreviewEnabled);
        if (!newStepPreviewEnabled) {
            stepButton.setText(messages.getString("steps.show"));
            stepButton.setStyle("-fx-text-fill: green;");
            unlock();

            Problem problem = application.getPlanningSession().getProblem();
            centerPaneController.setProblemSupplier(() -> application.getPlanningSessionOptional()
                    .map(PlanningSession::getProblem));
            problem.getRoadGraph().redrawActionObjectSprites(problem);
            planStateManager.set(null);
        } else {
            stepButton.setText(messages.getString("steps.hide"));
            stepButton.setStyle("-fx-text-fill: red;");
            lock();

            PlanningSession session = application.getPlanningSession();
            planStateManager.set(new TemporalPlanStateManager(session.getDomain(), session.getProblem(),
                    session.getPlan()));
            centerPaneController.setProblemSupplier(() -> Optional.ofNullable(planStateManager.get()
                    .getCurrentPlanState()));

            redrawState();
            updateTimeSpinner();
            updateTableSelection();
        }
    }

    /**
     * Handle pressing the show steps button. Toggles the "step showing" mode.
     */
    @FXML
    private void handleStepToggle() {
        changeSetPreview(!stepPreviewEnabled.get());
    }

    /**
     * Redraw sprites in the correct graph in the correct problem.
     */
    private void redrawState() {
        if (!stepPreviewEnabled.get()) {
            logger.info("Cannot draw step state when not enabled.");
            return;
        }
        stepUpdated.invalidated(null);
    }

    /**
     * Handle the "V" button press in the step button row. Go to the next action.
     */
    @FXML
    private void handleDownAction() {
        applyStartsButton.setSelected(false);
        actionTimeGroup.selectToggle(endTimeButton);
        planStateManager.get().goToNextCheckpoint();
        redrawState();
        updateTimeSpinner();
        updateTableSelection();
    }

    /**
     * Handle the "A" button press in the step button row. Go to the previous action.
     */
    @FXML
    private void handleUpAction() {
        applyStartsButton.setSelected(false);
        actionTimeGroup.selectToggle(endTimeButton);
        planStateManager.get().goToPreviousCheckpoint();
        redrawState();
        updateTimeSpinner();
        updateTableSelection();
    }

    /**
     * Handle the Start, Middle, End button presses.
     */
    @FXML
    private void handleTimeButtons() {
        applyStartsButton.setSelected(false);
        updateFromTimeButtons(null);
        redrawState();
        updateTimeSpinner();
        updateTableSelection();
    }

    /**
     * Handle the Apply starts button presses.
     */
    @FXML
    private void handleApplyStartsButton() {
        planStateManager.get().goToTime(planStateManager.get().getCurrentTime(), applyStartsButton.isSelected());
        redrawState();
        updateTimeSpinner();
        updateTableSelection();
    }

    /**
     * Updates the plan manager using the selected action as if changing the time button selection or
     * clicking on an action (takes into account time button selection for proper time to go to and whether
     * to apply starts).
     * <p>
     * If the selected action is null, tries to get the selected item from the table. If there is none, returns
     * without doing anything.
     *
     * @param selected the selected action
     */
    private void updateFromTimeButtons(TemporalPlanAction selected) {
        if (selected == null) {
            selected = actionTableFilter.getTableView().getSelectionModel().getSelectedItem();
        }
        if (selected == null) {
            logger.debug("No item selected, cannot update from time buttons.");
            return;
        }

        Toggle timeProperties = actionTimeGroup.getSelectedToggle();
        if (startTimeButton.equals(timeProperties)) {
            planStateManager.get().goToTime(selected.getStartTimestamp(), false);
        } else if (middleTimeButton.equals(timeProperties)) {
            planStateManager.get().goToTime((selected.getStartTimestamp()
                    + selected.getEndTimestamp()) / 2d, true);
        } else if (endTimeButton.equals(timeProperties)) {
            planStateManager.get().goToTime(selected.getEndTimestamp(), false);
        } else {
            throw new IllegalStateException("No time preference button selected.");
        }
    }

    /**
     * Update the selection in the plan table according to the plan state. Used only during step showing.
     */
    private void updateTableSelection() {
        MultipleSelectionModel<TemporalPlanAction> model = actionTableFilter.getTableView().getSelectionModel();
        model.clearSelection();

        Optional<TemporalPlanAction> toSelect = planStateManager.get().getLastAction();
        if (!toSelect.isPresent()) {
            return;
        }
        TemporalPlanAction actionToSelect = toSelect.get();

        int index = 0;
        for (TemporalPlanAction action : actionTableFilter.getFilteredList()) {
            if (actionToSelect.equals(action)) {
                thirdPartySelection = true;
                model.select(index);
            }
            index++;
        }
    }

    /**
     * Update the selection in the time spinner according to the plan state. Used only during step showing.
     */
    private void updateTimeSpinner() {
        Double currentTime = Optional.ofNullable(planStateManager.get()).map(PlanStateManager::getCurrentTime)
                .orElse(0d);
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0d,
                Double.MAX_VALUE, currentTime, 1d);
        factory.setConverter(new NumberDoubleStringConverter());
        thirdPartySpinnerChange = true;
        timeSpinner.setValueFactory(factory);
    }

    /**
     * Util method for overcoming the greedy linking of creating a lambda in the constructor of {@link IsNullBinding}.
     *
     * @return the planning session optional of the CDI injected application
     */
    private Optional<PlanningSession> getPlanningSessionOptionalIndirection() {
        return application.getPlanningSessionOptional();
    }

    /**
     * Immutable is null binding. Uses Optional's monad semantics to get a value inside the
     * optional and returns it.
     *
     * @param <T> the type of the optional
     */
    private static class OptionalSelectionBinding<T> extends BooleanBinding {

        private final Supplier<Optional<T>> supplier;
        private final Function<T, Boolean> getter;

        /**
         * Default constructor.
         *
         * @param supplier the optional supplier
         * @param getter the boolean getter
         */
        OptionalSelectionBinding(Supplier<Optional<T>> supplier, Function<T, Boolean> getter) {
            this.supplier = supplier;
            this.getter = getter;
        }

        @Override
        protected boolean computeValue() {
            return supplier.get().map(getter).orElse(true);
        }
    }

    /**
     * An extension of {@link OptionalSelectionBinding} for {@link PlanningSession}
     * using {@link TransportEditorApplication#getPlanningSessionOptional()}.
     * Is true iff the resulting getter value is null or any value on the way to it is null.
     */
    private class IsNullBinding extends OptionalSelectionBinding<PlanningSession> {
        /**
         * Default constructor.
         *
         * @param getter the getter of a property in the planning session
         */
        IsNullBinding(Function<PlanningSession, ObjectProperty> getter) {
            super(RightPaneController.this::getPlanningSessionOptionalIndirection,
                    getter.andThen(ObjectProperty::isNull).andThen(BooleanBinding::get));
        }
    }
}
