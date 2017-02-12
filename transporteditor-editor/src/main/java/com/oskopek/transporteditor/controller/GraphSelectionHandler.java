package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import javafx.beans.*;
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

public class GraphSelectionHandler implements javafx.beans.Observable {

    private static final String SELECTED = "ui.selected";
    private Problem problem;
    private final RoadGraph roadGraph;
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

    public GraphSelectionHandler(Problem problem, GraphicGraph graphicGraph) {
        this.problem = problem;
        this.roadGraph = problem.getRoadGraph();
        this.graphicGraph = graphicGraph;
        populateCollectionsFromGraph();
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    private void populateCollectionsFromGraph() {
        roadGraph.getAllLocations().filter(l -> roadGraph.getNode(l.getName()).hasAttribute(SELECTED))
                .forEach(selectedLocations::add);
        roadGraph.getAllRoads().map(RoadGraph.RoadEdge::getRoad)
                .filter(r -> roadGraph.getEdge(r.getName()).hasAttribute(SELECTED)).forEach(selectedRoads::add);
        problem.getAllPackages().stream().map(p -> Tuple.of(p, graphicGraph.getSprite("sprite-" + p.getName())))
                .filter(t -> t._2 != null && t._2.hasAttribute(SELECTED)).map(t -> t._1).forEach(selectedPackages::add);
        problem.getAllVehicles().stream().map(v -> Tuple.of(v, graphicGraph.getSprite("sprite-" + v.getName())))
                .filter(t -> t._2 != null && t._2.hasAttribute(SELECTED)).map(t -> t._1).forEach(selectedVehicles::add);
        selectedLocationList.addAll(selectedLocations);
        selectedRoadList.addAll(selectedRoads);
        selectedPackageList.addAll(selectedPackages);
        selectedVehicleList.addAll(selectedVehicles);
    }

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

    public void selectOnly(Node node) {
        unSelectAll();
        toggleSelectionOnElement(node);
    }

    public void selectOnly(Edge edge) {
        unSelectAll();
        toggleSelectionOnElement(graphicGraph.getSprite("sprite-" + edge.getId()));
    }

    public void selectOnly(ActionObject pkg) {
        unSelectAll();
        toggleSelectionOnElement(graphicGraph.getSprite("sprite-" + pkg.getName()));
    }

    public boolean doesSelectionDeterminePossibleNewRoad() {
        return selectedLocationList.size() == 2 && countSelectedElements() == 2;
    }

    public boolean doesSelectionDeterminePossibleNewPackage() {
//        return selectedLocationList.size() == 2 && countSelectedElements() == 2;
        return doesSelectionDeterminePossibleNewRoad();
    }

    public boolean doesSelectionDeterminePossibleNewVehicle() {
        return selectedLocationList.size() == 1 && countSelectedElements() == 1;
    }

    public boolean doesSelectionDetermineExistingRoads() {
        return doesSelectionDeterminePossibleNewRoad()
                && roadGraph.getAllRoadsBetween(selectedLocationList.get(0), selectedLocationList.get(1)).count() > 0;
    }

    public List<Location> getSelectedLocationList() {
        return selectedLocationList;
    }

    public List<Road> getSelectedRoadList() {
        return selectedRoadList;
    }

    public List<Package> getSelectedPackageList() {
        return selectedPackageList;
    }

    public List<Vehicle> getSelectedVehicleList() {
        return selectedVehicleList;
    }

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

    private static void removeSelectedAttribute(Element element) {
        element.removeAttribute(SELECTED);
    }

    private static void toggleSelectedAttribute(Element element) {
        if (element.hasAttribute(SELECTED)) {
            removeSelectedAttribute(element);
        } else {
            element.addAttribute(SELECTED);
        }
    }

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

    public void toggleSelectionOnElement(Element element) {
        toggleSelectedAttribute(element);
        String name = element.getId();
        if (element instanceof GraphicSprite && name.length() > "sprite-".length()) {
            GraphicSprite sprite = (GraphicSprite) element;
            String elementName = name.substring("sprite-".length());

            if (roadGraph.getRoad(elementName) != null) {
                Road road = roadGraph.getRoad(elementName);
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
            simultaneousUpdate(element, roadGraph.getLocation(name), selectedLocationList, selectedLocations);
        }

        logger.debug("Selected locations ({}), roads ({}), vehicles ({}) and packages ({}).", getSelectedLocationList(),
                getSelectedRoadList(), getSelectedVehicleList(), getSelectedPackageList());
    }

    public void toggleSelectionOnElements(Iterable<? extends Element> elements) {
        elements.forEach(this::toggleSelectionOnElement);
    }
}
