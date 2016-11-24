package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.view.plan.SequentialPlanList;
import com.oskopek.transporteditor.view.plan.TemporalPlanGanttChart;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    private Button cancelPlanButton;

    @Inject
    private EventBus eventBus;

    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    @Subscribe
    public void redrawPlans(PlanningFinishedEvent event) {
        logger.debug("Caught planning finished event: redrawing plans.");
        Platform.runLater(() -> {
            Plan plan = application.getPlanningSession().getPlan();
            if (plan == null) {
                linearPlanTabScrollPane.setContent(null);
                ganttPlanTabScrollPane.setContent(null);
            } else {
                linearPlanTabScrollPane.setContent(SequentialPlanList.build(plan.toTemporalPlan()));
                ganttPlanTabScrollPane.setContent(TemporalPlanGanttChart.build(plan.toTemporalPlan()));
            }

            cancelPlanButton.setDisable(true);
            planButton.setDisable(false);
        });
    }

    @FXML
    private void handlePlan() {
        logger.debug("Starting planning...");
        cancelPlanButton.setDisable(false);
        planButton.setDisable(true);
        application.getPlanningSession().startPlanning();
    }

    @FXML
    private void handleCancelPlan() {
        logger.debug("Stopping planning...");
        cancelPlanButton.setDisable(true);
        planButton.setDisable(false);
        application.getPlanningSession().stopPlanning();
        eventBus.post(new PlanningFinishedEvent());
    }

}
