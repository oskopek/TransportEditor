package com.oskopek.transporteditor.controller;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RightPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @FXML
    private SwingNode planGraph;

    @FXML
    private Button planButton;

    @FXML
    private Button cancelPlanButton;

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
    }

}
