package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.validation.Validator;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.InvalidableOrBooleanBinding;
import com.oskopek.transporteditor.view.LogProgressCreator;
import com.oskopek.transporteditor.view.plan.SequentialPlanTable;
import com.oskopek.transporteditor.view.plan.TemporalPlanTable;
import com.oskopek.transporteditor.view.plan.TemporalPlanGanttChart;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

@Singleton
public class RightPaneController extends AbstractController {

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

    @Inject
    private CenterPaneController centerPaneController;

    @Inject
    private EventBus eventBus;

    @FXML
    private void initialize() {
        eventBus.register(this);
        // TODO: Eliminate duplicate creation after binding is changed to be immutable

        // Disable plan button condition
        InvalidableOrBooleanBinding disablePlanButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::plannerProperty))
                .or(new IsNullBinding(PlanningSession::domainProperty)).or(
                        new IsNullBinding(PlanningSession::problemProperty));
        planButton.disableProperty().bind(disablePlanButton);

        // Disable validate button condition
        InvalidableOrBooleanBinding disableValidateButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty))
                .or(new IsNullBinding(PlanningSession::planProperty))
                .or(new IsNullBinding(PlanningSession::validatorProperty));
        validateButton.disableProperty().bind(disableValidateButton);

        // disable lock button condition
        InvalidableOrBooleanBinding disableLockButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty));
        lockButton.disableProperty().bind(disableLockButton);

        // Disable addLocation button condition
        InvalidableOrBooleanBinding disableAddLocationButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty))
                .or(new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return centerPaneController.isLocked();
                    }
                });
        centerPaneController.lockedProperty().addListener(e -> disableAddLocationButton.invalidate());
        addLocationButton.disableProperty().bind(disableAddLocationButton);

        // Disable redraw button condition
        InvalidableOrBooleanBinding disableRedrawButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull())
                .or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty))
                .or(new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return centerPaneController.isLocked();
                    }
                });
        redrawButton.disableProperty().bind(disableRedrawButton);

        // Disable graph changes (addVehicle) button condition
        InvalidableOrBooleanBinding disableAddVehicleButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty))
                .or(new OptionalSelectionBinding<>(
                        () -> Optional.ofNullable(centerPaneController.getGraphSelectionHandler()),
                        r -> !r.doesSelectionDeterminePossibleNewVehicle()))
                .or(new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return centerPaneController.isLocked();
                    }
                });
        addVehicleButton.disableProperty().bind(disableAddVehicleButton);

        // Disable graph changes (addRoad and addPackage) button condition
        InvalidableOrBooleanBinding disableAddRoadButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty))
                .or(new OptionalSelectionBinding<>(
                        () -> Optional.ofNullable(centerPaneController.getGraphSelectionHandler()),
                        r -> !r.doesSelectionDeterminePossibleNewRoad()))
                .or(new BooleanBinding() {
                    @Override
                    protected boolean computeValue() {
                        return centerPaneController.isLocked();
                    }
                });
        addRoadButton.disableProperty().bind(disableAddRoadButton);
        addPackageButton.disableProperty().bind(disableAddRoadButton);

        centerPaneController.lockedProperty().addListener(e -> {
            disableAddLocationButton.invalidate();
            disableAddVehicleButton.invalidate();
            disableAddRoadButton.invalidate();
            disableRedrawButton.invalidate();
        });

        // Update disable AddRoad and AddVehicle button
        InvalidationListener graphSelectionChangedListener = e -> {
            disableAddRoadButton.invalidate();
            disableAddVehicleButton.invalidate();
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
            disablePlanButton.invalidate();
            disableValidateButton.invalidate();
            disableRedrawButton.invalidate();
            disableLockButton.invalidate();
            disableAddVehicleButton.invalidate();
            disableAddRoadButton.invalidate();
            disableAddLocationButton.invalidate();
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

    @Subscribe
    public void redrawPlans(GraphUpdatedEvent event) {
        redrawPlansInternal();
    }

    @Subscribe
    public void redrawPlans(PlanningFinishedEvent event) {
        redrawPlansInternal();
    }

    private void redrawPlansInternal() {
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
                    actionTableFilter = TemporalPlanTable.build(plan.getTemporalPlanActions());
                    temporalPlanTabScrollPane.setContent(actionTableFilter.getTableView());
                    sequentialPlanTabScrollPane.setContent(null);
                    planTabPane.getSelectionModel().select(temporalPlanTab);
                } else {
                    actionTableFilter = SequentialPlanTable.build(plan.getTemporalPlanActions());
                    sequentialPlanTabScrollPane.setContent(actionTableFilter.getTableView());
                    temporalPlanTabScrollPane.setContent(null);
                    planTabPane.getSelectionModel().select(sequentialPlanTab);
                }

                lastChangeListener = c -> ganttPlanTabScrollPane.setContent(TemporalPlanGanttChart.build(c.getList()));
                actionTableFilter.getFilteredList().addListener(lastChangeListener);
                ganttPlanTabScrollPane.setContent(TemporalPlanGanttChart.build(actionTableFilter.getFilteredList()));

            }
        });
    }

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
                    + throwable.getMessage(), a -> application.centerInPrimaryStage(a, -200, -50), ButtonType.OK);
            return null;
        });
        logger.trace("LogProgress begin");
        logProgressCreator.createLogProgressDialog(application.getPlanningSession().getPlanner(), successful,
                completed.not(), completed.not(), planner::cancel);
        logger.trace("LogProgress end");
    }

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

    @FXML
    private void handleRedraw() {
        logger.debug("Starting redraw...");
        eventBus.post(new GraphUpdatedEvent());
    }

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
            vehicle = new Vehicle(name, at, ActionCost.valueOf(0), ActionCost.valueOf(0), ActionCost.valueOf(0),
                    ActionCost.valueOf(0), new ArrayList<>());
        } else {
            vehicle = new Vehicle(name, at, ActionCost.valueOf(0), ActionCost.valueOf(0), new ArrayList<>());
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

    @FXML
    private void handleLockToggle() {
        boolean newLocked = !centerPaneController.isLocked();
        centerPaneController.setLocked(newLocked);
        if (newLocked) {
            lockButton.setText(messages.getString("unlock"));
            lockButton.setStyle("-fx-text-fill: green;");
        } else {
            lockButton.setText(messages.getString("lock"));
            lockButton.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Is value not present or null?
     */
    private class IsNullBinding extends BooleanBinding {

        private final Function<PlanningSession, ObjectProperty> getter;

        IsNullBinding(Function<PlanningSession, ObjectProperty> getter) {
            this.getter = getter;
        }

        @Override
        protected boolean computeValue() {
            return application.getPlanningSessionOptional().map(getter).map(ObjectProperty::isNull).map(
                    BooleanBinding::get).orElse(true);
        }
    }

    private static class OptionalSelectionBinding<T> extends BooleanBinding {

        private final Supplier<Optional<T>> supplier;
        private final Function<T, Boolean> getter;

        OptionalSelectionBinding(Supplier<Optional<T>> supplier, Function<T, Boolean> getter) {
            this.supplier = supplier;
            this.getter = getter;
        }

        @Override
        protected boolean computeValue() {
            return supplier.get().map(getter).orElse(true);
        }
    }
}
