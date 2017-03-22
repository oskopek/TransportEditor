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
import javafx.stage.Stage;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Controller for the central pane (contains the graph rendering viewer).
 */
@Singleton
public class CenterPaneController extends AbstractController {

    @Inject
    private transient Logger logger;

    @Inject
    private transient ActionObjectDetailPopupCreator actionObjectDetailPopupCreator;

    @Inject
    private transient PropertyEditorDialogPaneCreator propertyEditorDialogPaneCreator;

    @FXML
    private transient SwingNode swingGraph;

    private transient Viewer viewer;

    private transient ObjectProperty<GraphSelectionHandler> graphSelectionHandler = new SimpleObjectProperty<>();

    private BooleanProperty locked = new SimpleBooleanProperty(false);

    private transient Supplier<Optional<Problem>> problemSupplier = () -> application.getPlanningSessionOptional()
            .map(PlanningSession::getProblem);

    /**
     * Get the problem supplier. Used for showing data in the graph (differentiating between the problem
     * and plan states when showing steps).
     *
     * @return the currently shown problem
     */
    public Supplier<Optional<Problem>> getProblemSupplier() {
        return problemSupplier;
    }

    /**
     * Set the problem supplier. Used for showing data in the graph (differentiating between the problem
     * and plan states when showing steps).
     *
     * @param problemSupplier supplier of the currently shown problem
     */
    public void setProblemSupplier(Supplier<Optional<Problem>> problemSupplier) {
        this.problemSupplier = problemSupplier;
    }

    /**
     * JavaFX initializer method. Registers with the event bus.
     */
    @FXML
    private void initialize() {
        eventBus.register(this);
    }

    /**
     * Determine if the graph is locked for edits.
     *
     * @return true iff the graph is locked
     */
    public boolean isLocked() {
        return locked.get();
    }

    /**
     * Set the graph lock.
     *
     * @param locked the new graph lock state
     */
    public void setLocked(boolean locked) {
        this.locked.set(locked);
    }

    /**
     * The graph's locked property.
     *
     * @return the locked property
     */
    public BooleanProperty lockedProperty() {
        return locked;
    }

    /**
     * Get the graph selection handler.
     *
     * @return the graph selection handler
     */
    public GraphSelectionHandler getGraphSelectionHandler() {
        return graphSelectionHandler.get();
    }

    /**
     * Get the graph selection handler property.
     *
     * @return the graph selection handler property
     */
    public ReadOnlyObjectProperty<GraphSelectionHandler> graphSelectionHandlerProperty() {
        return graphSelectionHandler;
    }

    /**
     * Sets the current problem instance into the graph selection handler.
     */
    public void refreshGraphSelectionHandler() {
        graphSelectionHandler.get().setProblem(application.getPlanningSession().getProblem());
    }

    /**
     * Disposes the graph viewer and nulls it out upon receiveing a {@link DisposeGraphViewerEvent}.
     *
     * @param event the event subscribed to
     */
    @Subscribe
    public void disposeGraphViewer(DisposeGraphViewerEvent event) {
        logger.debug("Disposing Graph viewer in CenterPane.");
        if (viewer != null) {
            viewer.close();
            viewer = null;
        }
        SwingUtilities.invokeLater(() -> {
            swingGraph.getContent().removeAll();
            swingGraph.setContent(null);
        });
    }

    /**
     * Redraws the graph and all related actions when a {@link GraphUpdatedEvent} is received.
     *
     * @param graphUpdatedEvent the event subscribed to
     */
    @Subscribe
    public void redrawGraph(GraphUpdatedEvent graphUpdatedEvent) {
        locked.setValue(false);
        RoadGraph graph = Try.of(() -> application.getPlanningSession().getProblem().getRoadGraph())
                .onFailure(e -> logger.trace("Could not get graph for redrawing, got a NPE along the way.", e))
                .getOrElse((RoadGraph) null);
        if (graph == null) {
            SwingUtilities.invokeLater(() ->
                    swingGraph.setContent(new JLabel(messages.getString("problem.noproblemloaded"))));
            return;
        }
        graph.setDefaultStyling();

        // TODO: move this to appropriate listener and possibly refactor
        Problem problem = application.getPlanningSession().getProblem();

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
                Platform.runLater(() -> swingGraph.requestFocus());
                SwingUtilities.invokeLater(() -> {
                    if (swingGraph.getContent() != null) {
                        swingGraph.getContent().requestFocus();
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
            newProblem.getRoadGraph().redrawActionObjectSprites(newProblem);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_FOCUSED);

        Platform.runLater(() -> {
            SwingUtilities.invokeLater(() -> {
                swingGraph.setContent(viewPanel);
                swingGraph.setDisable(false);
            });
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
                    graph.redrawActionObjectSprites(problem);
                    return null;
                }
            };
            Stage progressDialog = ProgressCreator.buildProgress(springLayoutEarlyTermination::progressProperty,
                    messages.getString("progress.pleaseWait"));
            application.centerInPrimaryStage(progressDialog, -50, -50);
            progressDialog.show();
            springLayoutEarlyTermination.setOnFailed(event -> {
                progressDialog.close();
                AlertCreator.showAlert(Alert.AlertType.ERROR,
                        messages.getString("root.failedToLayoutGraph") + ":\n\n" + event.getSource().getException(),
                        a -> application.centerInPrimaryStage(a, -200, -50));
            });
            springLayoutEarlyTermination.setOnSucceeded(event -> {
                progressDialog.close();
                Platform.runLater(viewer::disableAutoLayout);
            });
            CompletableFuture.runAsync(springLayoutEarlyTermination);
        });
    }

    /**
     * Custom mouse manager making sprites unmovable, respecting locking
     * and handling left and right mouse clicks according to the documentation.
     */
    private class SpriteUnClickableMouseManager extends DefaultMouseManager {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final GraphSelectionHandler selectionHandler;
        private ActionObjectDetailPopup popup;
        private RoadGraph graph;

        /**
         * Default constructor.
         *
         * @param selectionHandler the selection handler
         * @param graph the graph to manage
         */
        SpriteUnClickableMouseManager(GraphSelectionHandler selectionHandler, RoadGraph graph) {
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
            element.addAttribute("ui.clicked");
            if (SwingUtilities.isLeftMouseButton(event)) {
                if (event.isShiftDown()) {
                    selectionHandler.toggleSelectionOnElement(element);
                } else {
                    selectionHandler.unSelectAll();

                    popup = createResponse(problemSupplier.get().get(), element, actionObjectDetailPopupCreator);
                    if (popup != null) {
                        int showAtX = event.getXOnScreen();
                        int showAtY = event.getYOnScreen() - 15;
                        logger.trace("Showing popup at {}x{}", showAtX, showAtY);
                        Platform.runLater(() -> popup.show(swingGraph, showAtX, showAtY));
                    }
                }
            }
        }

        /**
         * Glue code method - passes the element to the appropriate creator with the proper
         * graph, problem and session update callbacks.
         *
         * @param problem the problem to take data from
         * @param element the element to pass
         * @param creator the creator to pass to
         * @param <T> the type of the created object
         * @return the created object
         */
        private <T> T createResponse(Problem problem, GraphicElement element,
                ActionObjectBuilderConsumer<? extends T> creator) {
            Consumer<Problem> problemUpdater = p -> {
                application.getPlanningSession().setProblem(p);
                // TODO: Hack
                application.getPlanningSession().getProblem().getRoadGraph().redrawActionObjectSprites(p);
            };
            if (element instanceof Node) { // TODO: Hack
                Location oldLocation = graph.getLocation(element.getId());
                return creator.create(oldLocation, newLocation -> problemUpdater.accept(problem
                        .changeLocation(oldLocation, newLocation)));
            } else if (element instanceof GraphicSprite) {
                String name = element.getId().substring("sprite-".length());
                RoadGraph.RoadEdge roadEdge = graph.getRoadEdge(name);
                if (roadEdge != null) {
                    return creator.create(roadEdge, newRoad -> graph.putRoad(newRoad, roadEdge.getFrom(),
                            roadEdge.getTo()));
                } else {
                    return creator.tryCreateFromLocatable(problem, name,
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
            view.freezeElement(element, false);
            element.removeAttribute("ui.clicked");
            if (SwingUtilities.isLeftMouseButton(event)) {
                if (popup != null) {
                    Platform.runLater(() -> popup.hide());
                }
            } else if (SwingUtilities.isRightMouseButton(event) && !locked.get()) {
                Supplier<Void> editDialog = createResponse(problemSupplier.get().get(), element,
                        propertyEditorDialogPaneCreator);
                Platform.runLater(editDialog::get);
            }

            // TODO: Update location in graph using moveLocation on drag
        }
    }
}
