package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.PlanningFinishedEvent;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.view.chart.TemporalPlanGanttChart;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RightPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @FXML
    private AnchorPane planTabAnchorPane;

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
    private void redrawPlans(PlanningFinishedEvent event) {
        logger.debug("Caught planning finished event: redrawing plans.");
        planTabAnchorPane.getChildren().set(0, null);
        TabPane tabPane = new TabPane();

        Plan plan = application.getPlanningSession().getPlan();
        tabPane.getTabs().add(new Tab("Gantt", TemporalPlanGanttChart.build(plan.toTemporalPlan()).rotate(90)));

        planTabAnchorPane.getChildren().add(tabPane);
    }

    @FXML
    private void handlePlan() {
        logger.debug("Starting model...");
        cancelPlanButton.setDisable(false);
        planButton.setDisable(true);
        application.getPlanningSession().startPlanning();
    }

    @FXML
    private void handleCancelPlan() {
        logger.debug("Stopping model...");
        cancelPlanButton.setDisable(true);
        planButton.setDisable(false);
        application.getPlanningSession().stopPlanning();
        eventBus.post(new PlanningFinishedEvent());
    }

}
