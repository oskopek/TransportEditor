/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.DefaultRoad;
import com.oskopek.transporteditor.planning.problem.Location;
import com.oskopek.transporteditor.planning.problem.RoadGraph;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import org.graphstream.ui.j2dviewer.J2DGraphRenderer;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

@Singleton
public class CenterPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @FXML
    private SwingNode problemGraph;

    private RoadGraph graph;

    @FXML
    private void initialize() { // TODO remove me
        RoadGraph testGraph = new RoadGraph("testGraph");

        Location a = new Location("A", 0, 0);
        Location b = new Location("B", 0, 1);
        Location c = new Location("C", 1, 0);

        testGraph.addLocation(c);
        testGraph.addLocation(a);
        testGraph.addLocation(b);

        testGraph.addRoad(new DefaultRoad("A->B", ActionCost.valueOf(1)), a, b);
        testGraph.addRoad(new DefaultRoad("B->A", ActionCost.valueOf(1)), b, a);
        testGraph.addRoad(new DefaultRoad("B->C", ActionCost.valueOf(2)), b, c);
        testGraph.addRoad(new DefaultRoad("C->A", ActionCost.valueOf(3)), c, a);

        loadProblemGraph(testGraph);
    }

    public void loadProblemGraph(RoadGraph graph) {
        this.graph = graph;
        redrawGraph();
    }

    private void redrawGraph() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                problemGraph.setContent(null);
                problemGraph.setDisable(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            logger.debug("An exception occurred while waiting for problemGraph to erase itself: {}", e);
        }

        if (graph != null) {
            Viewer viewer = graph.display();
            viewer.enableAutoLayout();
            ViewPanel viewPanel = viewer.addView("graph", new J2DGraphRenderer(), false);
            viewer.disableAutoLayout();
            SwingUtilities.invokeLater(() -> {
                problemGraph.setContent(viewPanel);
                problemGraph.setDisable(false);
            });
        }


    }


}
