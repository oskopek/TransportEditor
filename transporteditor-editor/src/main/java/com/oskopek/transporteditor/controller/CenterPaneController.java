package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.Subscribe;
import com.oskopek.transporteditor.event.DisposeGraphViewerEvent;
import com.oskopek.transporteditor.event.GraphUpdatedEvent;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.view.*;
import com.oskopek.transporteditor.view.plan.ActionObjectDetailPopup;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.*;
import javaslang.control.Try;
import org.graphstream.graph.Node;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
public class CenterPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @Inject
    private transient ActionObjectDetailPopupCreator actionObjectDetailPopupCreator;

    @Inject
    private transient PropertyEditorDialogPaneCreator propertyEditorDialogPaneCreator;

    @FXML
    private transient SwingNode problemGraph;

    private transient Viewer viewer;

    private transient ObjectProperty<GraphSelectionHandler> graphSelectionHandler = new SimpleObjectProperty<>();

    private BooleanProperty locked = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    public boolean isLocked() {
        return locked.get();
    }

    public BooleanProperty lockedProperty() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked.set(locked);
    }

    public GraphSelectionHandler getGraphSelectionHandler() {
        return graphSelectionHandler.get();
    }

    public ReadOnlyObjectProperty<GraphSelectionHandler> graphSelectionHandlerProperty() {
        return graphSelectionHandler;
    }

    public void refreshGraphSelectionHandler() {
        graphSelectionHandler.get().setProblem(application.getPlanningSession().getProblem());
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
        locked.setValue(false);
        Platform.runLater(() -> problemGraph.setContent(new JLabel(messages.getString("problem.noproblemloaded"))));

        RoadGraph graph = Try.of(() -> application.getPlanningSession().getProblem().getRoadGraph())
                .onFailure(e -> logger.trace("Could not get graph for redrawing, got a NPE along the way.", e))
                .getOrElse((RoadGraph) null);
        if (graph == null) {
            return;
        }
        graph.setDefaultStyling();

        // TODO: move this to appropriate listener and possibly refactor
        Problem problem = application.getPlanningSession().getProblem();
        graph.redrawActionObjectSprites(problem);

        disposeGraphViewer(null);
        final long nodeCount = graph.getNodeCount();
        viewer = graph.display(true);
        GraphicGraph graphicGraph = viewer.getGraphicGraph();
        graphSelectionHandler.setValue(new GraphSelectionHandler(problem, graphicGraph));
        ProxyPipe proxyPipe = viewer.newViewerPipe();
        proxyPipe.addAttributeSink(graph);
        ViewPanel viewPanel = viewer.addView("graph", new J2DGraphRenderer(), false);
        viewPanel.setMouseManager(new SpriteUnClickableMouseManager(getGraphSelectionHandler(), graph));
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

        viewPanel.registerKeyboardAction(e -> getGraphSelectionHandler().unSelectAll(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        viewPanel.registerKeyboardAction(e -> {
            List<Road> selectedRoads = new ArrayList<>(getGraphSelectionHandler().getSelectedRoadList());
            List<Location> selectedLocations = new ArrayList<>(getGraphSelectionHandler().getSelectedLocationList());
            List<Package> selectedPackages = new ArrayList<>(getGraphSelectionHandler().getSelectedPackageList());
            List<Vehicle> selectedVehicles = new ArrayList<>(getGraphSelectionHandler().getSelectedVehicleList());

            graphSelectionHandler.get().unSelectAll();

            Problem newProblem = application.getPlanningSession().getProblem();
            newProblem.getRoadGraph().removeRoads(selectedRoads.stream().map(Road::getName)::iterator);
            newProblem.getRoadGraph().removeLocations(selectedLocations);

            for (Package pkg : selectedPackages) {
                newProblem = newProblem.removePackage(pkg.getName());
            }
            for (Vehicle vehicle : selectedVehicles) {
                newProblem = newProblem.removeVehicle(vehicle.getName());
            }

            List<Package> conflictingPackages = newProblem.getAllPackages().stream()
                    .filter(p -> selectedLocations.contains(p.getLocation())).collect(Collectors.toList());
            List<Package> conflictingPackagesTarget = newProblem.getAllPackages().stream()
                    .filter(p -> selectedLocations.contains(p.getTarget())).collect(Collectors.toList());
            List<Vehicle> conflictingVehicles = newProblem.getAllVehicles().stream()
                    .filter(p -> selectedLocations.contains(p.getLocation())).collect(Collectors.toList());

            if (newProblem.getRoadGraph().getNodeCount() <= 0) {
                newProblem.getRoadGraph().addLocation(new Location("location0"));
            }

            Location newLoc = newProblem.getRoadGraph().getAllLocations().findAny().get();
            for (Package pkg : conflictingPackages) {
                newProblem = newProblem.changePackage(pkg, pkg.updateLocation(newLoc));
            }
            for (Package pkg : conflictingPackagesTarget) {
                newProblem = newProblem.changePackage(pkg, pkg.updateTarget(newLoc));
            }
            for (Vehicle vehicle : conflictingVehicles) {
                newProblem = newProblem.changeVehicle(vehicle, vehicle.updateLocation(newLoc));
            }
            application.getPlanningSession().setProblem(newProblem);
            graph.redrawActionObjectSprites(newProblem);
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
        private final GraphSelectionHandler selectionHandler;
        private ActionObjectDetailPopup popup;
        private RoadGraph graph;

        SpriteUnClickableMouseManager(GraphSelectionHandler selectionHandler, RoadGraph graph) {
            this.selectionHandler = selectionHandler;
            this.graph = graph;
        }

        @Override
        protected void mouseButtonPress(MouseEvent event) {
            if (locked.get()) {
                return;
            }
            view.requestFocus();
            // un-select all
            if (!event.isShiftDown()) {
                selectionHandler.unSelectAll();
            }
        }

        @Override
        protected void mouseButtonRelease(MouseEvent event, Iterable<GraphicElement> elementsInArea) {
            if (locked.get()) {
                return;
            }
            selectionHandler.toggleSelectionOnElements(elementsInArea);
        }

        @Override
        protected void mouseButtonPressOnElement(GraphicElement element, MouseEvent event) {
            if (locked.get()) {
                return;
            }
            view.freezeElement(element, true);
            element.addAttribute("ui.clicked");
            if (SwingUtilities.isLeftMouseButton(event)) {
                if (event.isShiftDown()) {
                    selectionHandler.toggleSelectionOnElement(element);
                } else {
                    selectionHandler.unSelectAll();

                    popup = createResponse(element, actionObjectDetailPopupCreator);
                    if (popup != null) {
                        int showAtX = event.getXOnScreen();
                        int showAtY = event.getYOnScreen() - 15;
                        logger.trace("Showing popup at {}x{}", showAtX, showAtY);
                        Platform.runLater(() -> popup.show(problemGraph, showAtX, showAtY));
                    }
                }
            }
        }

        private <T> T createResponse(GraphicElement element, ActionObjectBuilderConsumer<? extends T> creator) {
            Consumer<Problem> problemUpdater = problem -> {
                application.getPlanningSession().setProblem(problem);
                // TODO: Hack
                application.getPlanningSession().getProblem().getRoadGraph().redrawActionObjectSprites(problem);
            };
            PlanningSession session = application.getPlanningSession();
            if (element instanceof Node) { // TODO: Hack
                Location oldLocation = graph.getLocation(element.getId());
                return creator.create(oldLocation, newLocation -> problemUpdater.accept(session.getProblem()
                        .changeLocation(oldLocation, newLocation)));
            } else if (element instanceof GraphicSprite) {
                String name = element.getId().substring("sprite-".length());
                RoadGraph.RoadEdge roadEdge = graph.getRoadEdge(name);
                if (roadEdge != null) {
                    return creator.create(roadEdge, newRoad -> graph.putRoad(newRoad, roadEdge.getFrom(),
                            roadEdge.getTo()));
                } else {
                    return creator.tryCreateFromLocatable(application.getPlanningSession().getProblem(), name,
                            problemUpdater);
                }
            } else {
                return null;
            }
        }

        @Override
        protected void elementMoving(GraphicElement element, MouseEvent event) {
            if (locked.get()) {
                return;
            }
            if (SwingUtilities.isRightMouseButton(event)) {
                return; // do not let user move elements with right mouse button
            }
            if (!(element instanceof GraphicSprite)) {
                super.elementMoving(element, event); // prevents sprites from being moved
            }
            if (popup != null) {
                element.removeAttribute("ui.clicked");
                Platform.runLater(() -> popup.hide());
            }
        }

        @Override
        protected void mouseButtonReleaseOffElement(GraphicElement element, MouseEvent event) {
            if (locked.get()) {
                return;
            }
            view.freezeElement(element, false);
            element.removeAttribute("ui.clicked");
            if (SwingUtilities.isLeftMouseButton(event)) {
                if (popup != null) {
                    Platform.runLater(() -> popup.hide());
                }
            } else if (SwingUtilities.isRightMouseButton(event)) {
                Supplier<Void> editDialog = createResponse(element, propertyEditorDialogPaneCreator);
                Platform.runLater(editDialog::get);
            }

            // TODO: Update location in graph using moveLocation on drag
        }
    }
}
