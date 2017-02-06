package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.DisposeGraphViewerEvent;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.event.UpdatedGraphSelectionHandlerEvent;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.GraphActionObjectDetailPopupCreator;
import com.oskopek.transporteditor.view.ProgressCreator;
import com.oskopek.transporteditor.view.plan.GraphActionObjectDetailPopup;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.*;
import javaslang.control.Try;
import org.graphstream.graph.Node;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicSprite;
import org.graphstream.ui.j2dviewer.J2DGraphRenderer;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.DefaultMouseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CenterPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @Inject
    private transient GraphActionObjectDetailPopupCreator graphActionObjectDetailPopupCreator;

    @FXML
    private transient SwingNode problemGraph;

    private transient Viewer viewer;

    private transient RoadGraphSelectionHandler graphSelectionHandler;

    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    @Subscribe
    public void disposeGraphViewer(DisposeGraphViewerEvent event) {
        logger.debug("Disposing Graph viewer in CenterPane.");
        if (viewer != null) {
            viewer.close();
            viewer = null;
        }
        problemGraph.getContent().removeAll();
        problemGraph.setContent(null);
    }

    @Subscribe
    public void redrawGraph(GraphUpdatedEvent graphUpdatedEvent) {
        Platform.runLater(() -> problemGraph.setContent(new JLabel(messages.getString("problem.noproblemloaded"))));

        RoadGraph graph = Try.of(() -> application.getPlanningSession().getProblem().getRoadGraph())
                .onFailure(e -> logger.trace("Could not get graph for redrawing, got a NPE along the way.", e))
                .getOrElse((RoadGraph) null);
        if (graph == null) {
            return;
        }
        graph.setDefaultStyling();

        disposeGraphViewer(null);
        final long nodeCount = graph.getNodeCount();
        viewer = graph.display(true);
        graphSelectionHandler = new RoadGraphSelectionHandler(graph, viewer.getGraphicGraph());
        eventBus.post(new UpdatedGraphSelectionHandlerEvent(graphSelectionHandler));
        ProxyPipe proxyPipe = viewer.newViewerPipe();
        proxyPipe.addAttributeSink(graph);
        ViewPanel viewPanel = viewer.addView("graph", new J2DGraphRenderer(), false);
        viewPanel.setMouseManager(new SpriteUnClickableMouseManager(graphSelectionHandler, graph));
        viewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                Platform.runLater(() -> {
                        problemGraph.requestFocus();
                        if (problemGraph.getContent() != null) {
                            problemGraph.getContent().requestFocus();
                        }
                    });
                logger.trace("Requested focus on SwingNode.");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                proxyPipe.pump();
            }
        });

        viewPanel.registerKeyboardAction(e -> graphSelectionHandler.unSelectAll(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        viewPanel.registerKeyboardAction(e -> {
            List<Road> selectedRoads = new ArrayList<>(graphSelectionHandler.getSelectedRoadList());
            List<Location> selectedLocations = new ArrayList<>(graphSelectionHandler.getSelectedLocationList());
            graph.removeLocations(selectedLocations);
            graph.removeRoads(selectedRoads.stream().map(Road::getName)::iterator);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_FOCUSED);

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
            CompletableFuture.runAsync(springLayoutEarlyTermination);
        });
    }

    private class SpriteUnClickableMouseManager extends DefaultMouseManager {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final RoadGraphSelectionHandler selectionHandler;
        private GraphActionObjectDetailPopup popup;
        private RoadGraph graph;

        SpriteUnClickableMouseManager(RoadGraphSelectionHandler selectionHandler, RoadGraph graph) {
            this.selectionHandler = selectionHandler;
            this.graph = graph;
        }

        @Override
        protected void mouseButtonPress(MouseEvent event) {
            view.requestFocus();
            // un-select all
            if (!event.isShiftDown()) {
                selectionHandler.unSelectAll();
            }
        }

        @Override
        protected void mouseButtonRelease(MouseEvent event, Iterable<GraphicElement> elementsInArea) {
            selectionHandler.toggleSelectionOnElements(elementsInArea);
        }

        @Override
        protected void mouseButtonPressOnElement(GraphicElement element, MouseEvent event) {
            view.freezeElement(element, true);
            if (SwingUtilities.isLeftMouseButton(event)) {
                if (!event.isShiftDown()) {
                    selectionHandler.unSelectAll();
                }
                element.addAttribute("ui.clicked");
                selectionHandler.toggleSelectionOnElement(element);

                if (element instanceof Node) { // TODO: Hack
                    popup = graphActionObjectDetailPopupCreator.create(graph.getLocation(element.getId()));
                } else if (element instanceof GraphicSprite) {
                    String name = element.getId().substring("sprite-".length());
                    RoadGraph.RoadEdge roadEdge = graph.getRoadEdge(name);
                    if (roadEdge != null) {
                        popup = graphActionObjectDetailPopupCreator.create(roadEdge);
                    } else {
                        popup = graphActionObjectDetailPopupCreator.tryCreateFromLocatable(name);
                    }
                } else {
                    popup = null;
                }

                if (popup != null) {
                    int showAtX = event.getXOnScreen();
                    int showAtY = event.getYOnScreen() - Math.round((float) popup.getHeight()) - 30;
                    logger.trace("Showing popup at {}x{}", showAtX, showAtY);
                    Platform.runLater(() -> popup.show(application.getPrimaryStage(), showAtX, showAtY));
                }
            } else if (SwingUtilities.isRightMouseButton(event)) {
                logger.debug("Mouse right pressed at {}x{}", event.getXOnScreen(), event.getYOnScreen());
                // TODO: Open edit dialog
            }
        }

        @Override
        protected void elementMoving(GraphicElement element, MouseEvent event) {
            if (!(element instanceof GraphicSprite)) {
                super.elementMoving(element, event); // prevents sprites from being moved
            }
        }

        @Override
        protected void mouseButtonReleaseOffElement(GraphicElement element, MouseEvent event) {
            view.freezeElement(element, false);
            if (SwingUtilities.isLeftMouseButton(event)) {
                element.removeAttribute("ui.clicked");
                if (popup != null) {
                    Platform.runLater(() -> popup.hide());
                }
            }
        }


    }
}
