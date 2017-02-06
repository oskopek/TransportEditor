package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import javafx.beans.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicSprite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RoadGraphSelectionHandler implements javafx.beans.Observable {

    private static final String SELECTED = "ui.selected";
    private final RoadGraph roadGraph;
    private final GraphicGraph graphicGraph;
    private final Set<Location> selectedLocations = new TreeSet<>(Comparator.comparing(Location::getName));
    private final Set<Road> selectedRoads = new TreeSet<>(Comparator.comparing(Road::getName));
    private final ObservableList<Location> selectedLocationList = FXCollections.observableArrayList();
    private final ObservableList<Road> selectedRoadList = FXCollections.observableArrayList();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public RoadGraphSelectionHandler(RoadGraph roadGraph, GraphicGraph graphicGraph) {
        this.roadGraph = roadGraph;
        this.graphicGraph = graphicGraph;
        populateCollectionsFromGraph();
    }

    private void populateCollectionsFromGraph() {
        roadGraph.getAllLocations().filter(l -> roadGraph.getNode(l.getName()).hasAttribute(SELECTED))
                .forEach(selectedLocations::add);
        roadGraph.getAllRoads().map(RoadGraph.RoadEdge::getRoad)
                .filter(r -> roadGraph.getEdge(r.getName()).hasAttribute(SELECTED)).forEach(selectedRoads::add);
        selectedLocationList.addAll(selectedLocations);
        selectedRoadList.addAll(selectedRoads);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        selectedLocationList.addListener(listener);
        selectedRoadList.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        selectedLocationList.removeListener(listener);
        selectedRoadList.removeListener(listener);
    }

    public void selectOnly(Node node) {
        unSelectAll();
        toggleSelectionOnElement(node);
    }

    public void selectOnly(Edge edge) {
        unSelectAll();
        toggleSelectionOnElement(graphicGraph.getSprite("sprite-" + edge.getId()));
    }

    public boolean doesLocationSelectionDeterminePossibleNewRoad() {
        return selectedLocationList.size() == 2 && selectedRoadList.size() == 0;
    }

    public boolean doesLocationSelectionDetermineExistingRoads() {
        return doesLocationSelectionDeterminePossibleNewRoad()
                && roadGraph.getAllRoadsBetween(selectedLocationList.get(0), selectedLocationList.get(1)).count() > 0;
    }

    public List<Location> getSelectedLocationList() {
        return selectedLocationList;
    }

    public List<Road> getSelectedRoadList() {
        return selectedRoadList;
    }

    public void unSelectAll() {
        graphicGraph.forEach(RoadGraphSelectionHandler::removeSelectedAttribute);
        graphicGraph.getSpriteIterator().forEachRemaining(RoadGraphSelectionHandler::removeSelectedAttribute);

        selectedLocations.clear();
        selectedRoads.clear();
        selectedLocationList.clear();
        selectedRoadList.clear();
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
        if (element instanceof GraphicSprite) {
            GraphicSprite sprite = (GraphicSprite) element;
            Edge edge = sprite.getEdgeAttachment();
            if (edge != null) {
                simultaneousUpdate(element, roadGraph.getRoad(edge.getId()), selectedRoadList, selectedRoads);
            }
        } else if (element instanceof Node) {
            simultaneousUpdate(element, roadGraph.getLocation(name), selectedLocationList, selectedLocations);
        }
        logger.trace("Selected locations ({}) and roads ({})", getSelectedLocationList(), getSelectedRoadList());
    }

    public void toggleSelectionOnElements(Iterable<? extends Element> elements) {
        elements.forEach(this::toggleSelectionOnElement);
    }
}
