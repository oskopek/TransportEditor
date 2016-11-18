package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import org.graphstream.ui.j2dviewer.J2DGraphRenderer;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

@Singleton
public class CenterPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @FXML
    private SwingNode problemGraph;

    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    @Subscribe
    public void redrawGraph(GraphUpdatedEvent event) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                problemGraph.setContent(null);
                problemGraph.setDisable(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            logger.debug("An exception occurred while waiting for problemGraph to erase itself: {}", e);
        }

        RoadGraph graph = null;
        try {
            graph = application.getPlanningSession().getProblem().getRoadGraph();
        } catch (NullPointerException e) {
            logger.trace("Could not get graph for redrawing, got a NPE along the way.");
        }

        if (graph == null) {
            return;
        }
        Viewer viewer = graph.display();
        viewer.enableAutoLayout();
        ViewerPipe mousePipe = viewer.newViewerPipe();
        mousePipe.addViewerListener(new MouseCatcher());
        ViewPanel viewPanel = viewer.addView("graph", new J2DGraphRenderer(), false);
        //        viewer.disableAutoLayout();
        viewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                mousePipe.pump();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                mousePipe.pump();
            }
        });
        SwingUtilities.invokeLater(() -> {
            problemGraph.setContent(viewPanel);
            problemGraph.setDisable(false);
        });
    }

    private static class MouseCatcher implements ViewerListener {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void viewClosed(String s) {
            logger.debug("Closed view \"{}\"", s);
        }

        @Override
        public void buttonPushed(String s) {
            logger.debug("Pushed node \"{}\"", s);
        }

        @Override
        public void buttonReleased(String s) {
            logger.debug("Released node \"{}\"", s);
        }
    }

}
