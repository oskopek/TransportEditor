package com.oskopek.transporteditor.controller;

import com.oskopek.transport.model.problem.Package;
import javafx.beans.*;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javaslang.Tuple;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicSprite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages user selection of elements in the graph. Supports roads, locations, packages and vehicles.
 */
public class GraphSelectionHandler implements Observable {

    private static final String SELECTED = "ui.selected";
    private Problem problem;
    private final GraphicGraph graphicGraph;
    private final Set<Location> selectedLocations = new TreeSet<>(Comparator.comparing(Location::getName));
    private final Set<Road> selectedRoads = new TreeSet<>(Comparator.comparing(Road::getName));
    private final Set<Package> selectedPackages = new TreeSet<>(Comparator.comparing(Package::getName));
    private final Set<Vehicle> selectedVehicles = new TreeSet<>(Comparator.comparing(Vehicle::getName));
    private final ObservableList<Location> selectedLocationList = FXCollections.observableArrayList();
    private final ObservableList<Road> selectedRoadList = FXCollections.observableArrayList();
    private final ObservableList<Package> selectedPackageList = FXCollections.observableArrayList();
    private final ObservableList<Vehicle> selectedVehicleList = FXCollections.observableArrayList();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor.
     *
     * @param problem the problem we are selecting action objects from
     * @param graphicGraph the graphic graph instance (from the viewer)
     */
    public GraphSelectionHandler(Problem problem, GraphicGraph graphicGraph) {
        this.problem = problem;
        this.graphicGraph = graphicGraph;
        populateCollectionsFromGraph();
    }

    /**
     * Set the problem instance.
     *
     * @param problem the problem
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    /**
     * Populates all selection sets and list from the graph instance.
     */
    private void populateCollectionsFromGraph() {
        problem.getRoadGraph().getAllLocations().filter(l -> problem.getRoadGraph().getNode(l.getName())
                .hasAttribute(SELECTED)).forEach(selectedLocations::add);
        problem.getRoadGraph().getAllRoads().map(RoadGraph.RoadEdge::getRoad).filter(r -> problem.getRoadGraph()
                .getEdge(r.getName()).hasAttribute(SELECTED)).forEach(selectedRoads::add);
        problem.getAllPackages().stream().map(p -> Tuple.of(p, graphicGraph.getSprite("sprite-" + p.getName())))
                .filter(t -> t._2 != null && t._2.hasAttribute(SELECTED)).map(t -> t._1).forEach(selectedPackages::add);
        problem.getAllVehicles().stream().map(v -> Tuple.of(v, graphicGraph.getSprite("sprite-" + v.getName())))
                .filter(t -> t._2 != null && t._2.hasAttribute(SELECTED)).map(t -> t._1).forEach(selectedVehicles::add);
        selectedLocationList.addAll(selectedLocations);
        selectedRoadList.addAll(selectedRoads);
        selectedPackageList.addAll(selectedPackages);
        selectedVehicleList.addAll(selectedVehicles);
    }

    /**
     * Count the selected elements across categories.
     *
     * @return the sum of sizes of selected element sets
     */
    private int countSelectedElements() {
        return selectedVehicleList.size() + selectedPackageList.size() + selectedRoadList.size()
                + selectedLocationList.size();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        selectedLocationList.addListener(listener);
        selectedRoadList.addListener(listener);
        selectedPackageList.addListener(listener);
        selectedVehicleList.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        selectedLocationList.removeListener(listener);
        selectedRoadList.removeListener(listener);
        selectedPackageList.removeListener(listener);
        selectedVehicleList.removeListener(listener);
    }

    /**
     * De-select everything and select only the given node.
     *
     * @param node the node to be selected
     */
    public void selectOnly(Node node) {
        unSelectAll();
        toggleSelectionOnElement(node);
    }

    /**
     * De-select everything and select only the given edge.
     *
     * @param edge the edge to be selected
     */
    public void selectOnly(Edge edge) {
        unSelectAll();
        toggleSelectionOnElement(graphicGraph.getSprite("sprite-" + edge.getId()));
    }

    /**
     * De-select everything and select only the given action object.
     *
     * @param actionObject the action object to be selected
     */
    public void selectOnly(ActionObject actionObject) {
        unSelectAll();
        toggleSelectionOnElement(graphicGraph.getSprite("sprite-" + actionObject.getName()));
    }

    /**
     * Does the current selection determine a possible new road in the graph?
     *
     * @return true iff it does
     */
    public boolean doesSelectionDeterminePossibleNewRoad() {
        return selectedLocationList.size() == 2 && countSelectedElements() == 2;
    }

    /**
     * Does the current selection determine a possible new package in the graph?
     *
     * @return true iff it does
     */
    public boolean doesSelectionDeterminePossibleNewPackage() {
//        return selectedLocationList.size() == 2 && countSelectedElements() == 2;
        return doesSelectionDeterminePossibleNewRoad();
    }

    /**
     * Does the current selection determine a possible new vehicle in the graph?
     *
     * @return true iff it does
     */
    public boolean doesSelectionDeterminePossibleNewVehicle() {
        return selectedLocationList.size() == 1 && countSelectedElements() == 1;
    }

    /**
     * Does the current selection determine (an) existing road(s) in the graph?
     *
     * @return true iff it does
     */
    public boolean doesSelectionDetermineExistingRoads() {
        return doesSelectionDeterminePossibleNewRoad() && problem.getRoadGraph()
                .getAllRoadsBetween(selectedLocationList.get(0), selectedLocationList.get(1)).count() > 0;
    }

    /**
     * Get the selected location list. Preserves selection order.
     *
     * @return the selected location list
     */
    public List<Location> getSelectedLocationList() {
        return selectedLocationList;
    }

    /**
     * Get the selected road list. Preserves selection order.
     *
     * @return the selected road list
     */
    public List<Road> getSelectedRoadList() {
        return selectedRoadList;
    }

    /**
     * Get the selected package list. Preserves selection order.
     *
     * @return the selected package list
     */
    public List<Package> getSelectedPackageList() {
        return selectedPackageList;
    }

    /**
     * Get the selected vehicle list. Preserves selection order.
     *
     * @return the selected vehicle list
     */
    public List<Vehicle> getSelectedVehicleList() {
        return selectedVehicleList;
    }

    /**
     * De-select everything.
     */
    public void unSelectAll() {
        graphicGraph.forEach(GraphSelectionHandler::removeSelectedAttribute);
        graphicGraph.getSpriteIterator().forEachRemaining(GraphSelectionHandler::removeSelectedAttribute);

        selectedLocations.clear();
        selectedRoads.clear();
        selectedPackages.clear();
        selectedVehicles.clear();
        selectedLocationList.clear();
        selectedRoadList.clear();
        selectedPackageList.clear();
        selectedVehicleList.clear();
        logger.trace("Unselected all.");
    }

    /**
     * Remove the "selected" attribute from an element of the graphic graph.
     *
     * @param element the element to de-select in the view
     */
    private static void removeSelectedAttribute(Element element) {
        element.removeAttribute(SELECTED);
    }

    /**
     * Toggle the "selected" attribute of an element of the graphic graph.
     *
     * @param element the element to (de)select in the view
     */
    private static void toggleSelectedAttribute(Element element) {
        if (element.hasAttribute(SELECTED)) {
            removeSelectedAttribute(element);
        } else {
            element.addAttribute(SELECTED);
        }
    }

    /**
     * Simultaneously update the selection set and list in the internal state according to an elements
     * selection attribute.
     *
     * @param element the element to check for updates
     * @param object the object to add/remove in case we detect an update
     * @param list the selection list reference
     * @param set the selection set reference
     * @param <T> the type of the object added/removed/not updated
     */
    private static <T> void simultaneousUpdate(Element element, T object, List<T> list, Set<T> set) {
        if (element.hasAttribute(SELECTED)) {
            if (set.add(object)) {
                list.add(object);
            }
        } else {
            if (set.remove(object)) {
                list.remove(object);
            }
        }
    }

    /**
     * Toggle the selection of an element of the graph. Tries to determine the type of the element
     * and toggle its selection, updating the internal selection state and the view.
     *
     * @param element the element to (de)select
     */
    public void toggleSelectionOnElement(Element element) {
        toggleSelectedAttribute(element);
        String name = element.getId();
        if (element instanceof GraphicSprite && name.length() > "sprite-".length()) {
            GraphicSprite sprite = (GraphicSprite) element;
            String elementName = name.substring("sprite-".length());

            if (problem.getRoadGraph().getRoad(elementName) != null) {
                Road road = problem.getRoadGraph().getRoad(elementName);
                simultaneousUpdate(element, road, selectedRoadList, selectedRoads);
            } else if (problem.getPackage(elementName) != null) {
                Package pkg = problem.getPackage(elementName);
                simultaneousUpdate(element, pkg, selectedPackageList, selectedPackages);
            } else if (problem.getVehicle(elementName) != null) {
                Vehicle pkg = problem.getVehicle(elementName);
                simultaneousUpdate(element, pkg, selectedVehicleList, selectedVehicles);
            } else {
                throw new IllegalStateException("Unknown element type, cannot select.");
            }
        } else if (element instanceof Node) {
            simultaneousUpdate(element, problem.getRoadGraph().getLocation(name), selectedLocationList,
                    selectedLocations);
        }

        logger.debug("Selected locations ({}), roads ({}), vehicles ({}) and packages ({}).", getSelectedLocationList(),
                getSelectedRoadList(), getSelectedVehicleList(), getSelectedPackageList());
    }

    /**
     * Toggle selection of multiple elements.
     *
     * @param elements the elements to toggle selection on
     * @see #toggleSelectionOnElement(Element)
     */
    public void toggleSelectionOnElements(Iterable<? extends Element> elements) {
        elements.forEach(this::toggleSelectionOnElement);
    }
}
