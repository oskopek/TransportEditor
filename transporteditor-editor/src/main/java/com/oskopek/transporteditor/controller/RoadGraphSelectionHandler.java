package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import org.graphstream.graph.Element;

import java.util.*;

public class RoadGraphSelectionHandler {

    private static final String SELECTED = "selected";
    private final RoadGraph roadGraph;
    private final SortedSet<Location> selectedLocations = new TreeSet<>(Comparator.comparing(Location::getName));
    private final SortedSet<Road> selectedRoads = new TreeSet<>(Comparator.comparing(Road::getName));

    public RoadGraphSelectionHandler(RoadGraph roadGraph) {
        this.roadGraph = roadGraph;
        populateSetsFromGraph();
    }

    private void populateSetsFromGraph() {
        roadGraph.getAllLocations().filter(l -> roadGraph.getNode(l.getName()).hasAttribute(SELECTED))
                .forEach(selectedLocations::add);
        roadGraph.getAllRoads().map(RoadGraph.RoadEdge::getRoad)
                .filter(r -> roadGraph.getEdge(r.getName()).hasAttribute(SELECTED)).forEach(selectedRoads::add);
    }

    private static <T extends Element, U> void toggleSelectInternal(U original, T object, Set<U> selectedObjList) {
        boolean newSelected = !object.hasAttribute(SELECTED);
        if (newSelected) {
            selectedObjList.add(original);
            object.addAttribute(SELECTED);
            object.addAttribute("ui.class", "selected");
        } else {
            selectedObjList.remove(original);
            object.removeAttribute(SELECTED);
            object.removeAttribute("ui.class");
        }
    }

    public boolean doesSelectionDetermineNewRoad() {
        return selectedLocations.size() == 2 &&
                roadGraph.getAllRoadsBetween(selectedLocations.first(), selectedLocations.last()).count() == 0;
    }

    public void toggleSelectLocation(String name) {
        toggleSelectInternal(roadGraph.getLocation(name), roadGraph.getNode(name), selectedLocations);
    }

    public void toggleSelectLocation(Location location) {
        toggleSelectInternal(location, roadGraph.getNode(location.getName()), selectedLocations);
    }

    public void toggleSelectRoad(String name) {
        toggleSelectInternal(roadGraph.getRoad(name), roadGraph.getEdge(name), selectedRoads);
    }

    public void toggleSelectRoad(Road road) {
        toggleSelectInternal(road, roadGraph.getEdge(road.getName()), selectedRoads);
    }

    public void unSelectAllLocations() {
        selectedLocations.forEach(this::toggleSelectLocation);
        selectedLocations.clear();
    }
    public void unSelectAllRoads() {
        selectedRoads.forEach(this::toggleSelectRoad);
        selectedRoads.clear();
    }
    public void unselectAll() {
        unSelectAllLocations();
        unSelectAllRoads();
    }

    public SortedSet<Location> getSelectedLocations() {
        return selectedLocations;
    }

    public SortedSet<Road> getSelectedRoads() {
        return selectedRoads;
    }
}
