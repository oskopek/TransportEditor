package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.InvalidableOrBooleanBinding;
import com.oskopek.transporteditor.view.plan.SequentialPlanList;
import com.oskopek.transporteditor.view.plan.TemporalPlanGanttChart;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
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

    @FXML
    private ScrollPane linearPlanTabScrollPane;

    @FXML
    private ScrollPane ganttPlanTabScrollPane;

    @FXML
    private Button planButton;

    @FXML
    private Button validateButton;

    @Inject
    private EventBus eventBus;

    @FXML
    private void initialize() {
        eventBus.register(this);
        InvalidableOrBooleanBinding disablePlanButton = new InvalidableOrBooleanBinding(
                application.planningSessionProperty().isNull()).or(new IsNullBinding(PlanningSession::plannerProperty))
                .or(new IsNullBinding(PlanningSession::domainProperty)).or(
                        new IsNullBinding(PlanningSession::problemProperty));
        planButton.disableProperty().bind(disablePlanButton);
        validateButton.disableProperty().bind(
                disablePlanButton.or(new IsNullBinding(PlanningSession::validatorProperty)));
        InvalidationListener invalidatePlanButtonBindingListener = s -> {
            disablePlanButton.invalidate();
            planButton.setDisable(disablePlanButton.get());
            validateButton.setDisable(disablePlanButton.get());
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
        planButton.setDisable(true);
        CompletionStage<Plan> planFuture = application.getPlanningSession().startPlanningAsync();
        planFuture.thenRun(() -> Platform.runLater(() -> {
            planButton.setDisable(false);
            eventBus.post(new PlanningFinishedEvent());
        })).exceptionally(throwable -> {
            logger.warn("Planning failed.", throwable);
            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("planning.failed") + ": "
                    + throwable.getMessage(), ButtonType.OK);
            return null;
        });
    }

    @FXML
    private void handleValidate() {
        logger.debug("Starting validation...");
        planButton.setDisable(true);

        CompletionStage<Boolean> validationFuture = application.getPlanningSession().startValidationAsync();
        validationFuture.thenAccept(isValid -> {
            Platform.runLater(() -> planButton.setDisable(false));
            if (isValid) {
                AlertCreator.showAlert(Alert.AlertType.INFORMATION, messages.getString("validation.valid"),
                        ButtonType.CLOSE);
            } else {
                AlertCreator
                        .showAlert(Alert.AlertType.ERROR, messages.getString("validation.invalid"), ButtonType.CLOSE);
            }
        });
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
