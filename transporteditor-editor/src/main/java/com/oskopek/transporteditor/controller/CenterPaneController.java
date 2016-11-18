package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.ProgressCreator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.graphstream.ui.j2dviewer.J2DGraphRenderer;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Singleton
public class CenterPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @Inject
    private transient ProgressCreator progressCreator;

    @FXML
    private SwingNode problemGraph;

    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    @Subscribe
    public void redrawGraph(GraphUpdatedEvent graphUpdatedEvent) {
        Platform.runLater(() -> {
            problemGraph.setContent(null);
            problemGraph.setDisable(true);
        });

        RoadGraph graph = null;
        try {
            graph = application.getPlanningSession().getProblem().getRoadGraph();
        } catch (NullPointerException e) {
            logger.trace("Could not get graph for redrawing, got a NPE along the way.");
        }

        if (graph == null) {
            return;
        }
        final long nodeCount = graph.getNodeCount();
        Viewer viewer = graph.display();
        viewer.enableAutoLayout();
        ViewerPipe mousePipe = viewer.newViewerPipe();
        mousePipe.addViewerListener(new MouseCatcher());
        ViewPanel viewPanel = viewer.addView("graph", new J2DGraphRenderer(), false);
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
        Platform.runLater(() -> {
            problemGraph.setContent(viewPanel);
            problemGraph.setDisable(false);
            Task<Void> springLayoutEarlyTermination = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    final long start = 2000L;
                    final long step = 50L;
                    final long total = start + nodeCount * step;
                    updateProgress(0, total);
                    try {
                        Thread.sleep(start / 2);
                        updateProgress(start / 2, total);
                        Thread.sleep(start / 2);
                        updateProgress(start, total);
                        for (int i = 0; i < nodeCount; i++) {
                            Thread.sleep(step);
                            updateProgress(start + step * i, total);
                        }
                        updateProgress(total, total);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("Sleep broken.", e);
                    }
                    logger.debug("Killing spring layout early ({}ms).", total);
                    return null;
                }
            };
            Stage progressDialog = ProgressCreator.showProgress(springLayoutEarlyTermination::progressProperty,
                    messages.getString("progress.pleaseWait"));
            springLayoutEarlyTermination.setOnFailed(event -> {
                progressDialog.close();
                AlertCreator.showAlert(Alert.AlertType.ERROR,
                        messages.getString("root.failedToLayoutGraph") + ":\n\n" + event.getSource().getException());
            });
            springLayoutEarlyTermination.setOnSucceeded(event -> {
                progressDialog.close();
                Platform.runLater(viewer::disableAutoLayout);
            });

            new Thread(springLayoutEarlyTermination).start();
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
