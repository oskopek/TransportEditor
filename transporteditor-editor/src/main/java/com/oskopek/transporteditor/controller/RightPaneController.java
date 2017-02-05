package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.DefaultRoad;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.validation.Validator;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.InvalidableOrBooleanBinding;
import com.oskopek.transporteditor.view.LogProgressCreator;
import com.oskopek.transporteditor.view.plan.SequentialPlanList;
import com.oskopek.transporteditor.view.plan.TemporalPlanGanttChart;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Singleton
public class RightPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @Inject
    private LogProgressCreator logProgressCreator;

    @FXML
    private ScrollPane linearPlanTabScrollPane;

    @FXML
    private ScrollPane ganttPlanTabScrollPane;

    @FXML
    private Button planButton;

    @FXML
    private Button validateButton;

    @FXML
    private Button addLocationButton;

    @FXML
    private Button addRoadButton;

    @FXML
    private Button addVehicleButton;

    @FXML
    private Button addPackageButton;

    @Inject
    private EventBus eventBus;

    @FXML
    private void initialize() {
        eventBus.register(this);
        // TODO: Eliminate duplicate creation after binding is changed to be immutable
        InvalidableOrBooleanBinding disablePlanButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::plannerProperty))
                .or(new IsNullBinding(PlanningSession::domainProperty)).or(
                        new IsNullBinding(PlanningSession::problemProperty));
        planButton.disableProperty().bind(disablePlanButton);

        InvalidableOrBooleanBinding disableValidateButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty))
                .or(new IsNullBinding(PlanningSession::planProperty))
                .or(new IsNullBinding(PlanningSession::validatorProperty));
        validateButton.disableProperty().bind(disableValidateButton);

        InvalidableOrBooleanBinding disableGraphChangeButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty));
        addLocationButton.disableProperty().bind(disableGraphChangeButton);
        addPackageButton.disableProperty().bind(disableGraphChangeButton);
        addVehicleButton.disableProperty().bind(disableGraphChangeButton);

        InvalidableOrBooleanBinding disableAddRoadButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::domainProperty))
                .or(new IsNullBinding(PlanningSession::problemProperty));
                //.or(not exactly 2 nodes selected); // TODO OOO when selection is implemented
        addRoadButton.disableProperty().bind(disableAddRoadButton);

        InvalidationListener invalidatePlanButtonBindingListener = s -> {
            disablePlanButton.invalidate();
            disableValidateButton.invalidate();
            disableGraphChangeButton.invalidate();
            disableAddRoadButton.invalidate();
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

            if (plan == null) {
                linearPlanTabScrollPane.setContent(null);
                ganttPlanTabScrollPane.setContent(null);
            } else {
                linearPlanTabScrollPane.setContent(SequentialPlanList.build(plan.toTemporalPlan()));
                ganttPlanTabScrollPane.setContent(TemporalPlanGanttChart.build(plan.toTemporalPlan()));
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
                            + messages.getString("planner.nullplan"), ButtonType.OK);
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
                    + throwable.getMessage(), ButtonType.OK);
            return null;
        });
        logger.trace("LogProgress begin");
        logProgressCreator.createLogProgressDialog(application.getPlanningSession().getPlanner(), successful,
                completed.not().or(successful.not()), completed.not(), planner::cancel);
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
                        ButtonType.CLOSE);
            } else {
                AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("validation.invalid"),
                        ButtonType.CLOSE);
            }
        })).exceptionally(throwable -> {
            Platform.runLater(() -> completed.setValue(true));
            AlertCreator.showAlert(Alert.AlertType.ERROR,
                    messages.getString("validation.failed") + ": " + throwable.getMessage(), ButtonType.CLOSE);
            return null;
        }).toCompletableFuture();
        logProgressCreator.createLogProgressDialog(application.getPlanningSession().getValidator(), successful,
                completed.not().or(successful.not()), completed.not(), validator::cancel);
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
                    ButtonType.CLOSE);
        } else {
            graph.addLocation(new Location("loc" + graph.getNodeCount()));
            // TODO OOO Select the added location or open its edit dialog
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
                    ButtonType.CLOSE);
        } else {
            // TODO OOO get first and second selected location
            Location from = null;
            Location to = null;
            graph.addRoad(new DefaultRoad("road" + graph.getEdgeCount(), ActionCost.valueOf(1)), from, to);
            // TODO OOO Select the added road
        }
    }

    @FXML
    private void handleAddVehicle() {
        // TODO OOO open add vehicle dialog
    }

    @FXML
    private void handleAddPackage() {
        // TODO OOO open add vehicle dialog
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
}
