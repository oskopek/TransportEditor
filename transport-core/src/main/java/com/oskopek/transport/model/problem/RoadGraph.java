package com.oskopek.transport.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper interface around a GraphStream oriented {@link MultiGraph} type. Currently supports only a single
 * oriented edge between two ordered nodes (i.e. a total of two edges between a set of two nodes).
 */
public class RoadGraph extends MultiGraph implements Graph {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Copy constructor.
     *
     * @param graph the graph to copy
     */
    public RoadGraph(RoadGraph graph) {
        this(graph.getId());
        graph.getAllLocations().forEach(this::addLocation);
        graph.getAllRoads().forEach(e -> this.addRoad(e.getRoad(), e.getFrom(), e.getTo()));
    }

    /**
     * Constructs a new empty graph.
     *
     * @param id the name of the graph
     */
    public RoadGraph(String id) {
        super(id);
    }

    /**
     * Add a new node to the graph representing the location.
     *
     * @param location the location to add
     * @param <T> the type of the node object
     * @return the created node
     */
    public <T extends Node> T addLocation(Location location) {
        addAttribute(location.getName(), location);
        T node = addNode(location.getName());
        node.addAttribute("ui.label", location.getName());
        return node;
    }

    /**
     * Removes the location and associated node from the graph. Does checking and removes any
     * associated roads too.
     *
     * @param location the location to remove
     */
    public void removeLocation(Location location) {
        removeRoads(getAllRoads().filter(re -> re.getFrom().equals(location) || re.getTo().equals(location))
                .map(r -> r.getRoad().getName()).collect(Collectors.toList()));
        removeNode(location.getName());
        removeAttribute(location.getName());
    }

    /**
     * Removes all the locations.
     *
     * @param locations the locations to remove
     * @see #removeLocation(Location)
     */
    public void removeLocations(Iterable<? extends Location> locations) {
        locations.forEach(this::removeLocation);
    }

    /**
     * Move the given location to new X and Y coordinates.
     *
     * @param name the name of the location to move
     * @param newX the new X coordinate
     * @param newY the new Y coordinate
     * @return the updated location
     */
    public Location moveLocation(String name, int newX, int newY) {
        Location original = getAttribute(name);
        Location newLocation = new Location(original.getName(), newX, newY);
        setAttribute(newLocation.getName(), newLocation);
        return original;
    }

    /**
     * Update the location's petrol station property. Creates a new location instance and replaces
     * it in the graph.
     *
     * @param locationName the location's name
     * @param hasPetrolStation the new petrol station value
     * @return the updated location
     */
    public Location setPetrolStation(String locationName, boolean hasPetrolStation) {
        Location original = getAttribute(locationName);
        Location newLocation = original.updateHasPetrolStation(hasPetrolStation);
        setAttribute(newLocation.getName(), newLocation);
        return original;
    }

    /**
     * Get the location of the given name.
     *
     * @param name the name of the location
     * @return the location, or null if there is no such location
     */
    public Location getLocation(String name) {
        return getAttribute(name);
    }

    /**
     * Get a stream of all the locations in the graph.
     *
     * @return a stream of all the locations
     */
    public Stream<Location> getAllLocations() {
        Stream.Builder<Location> stream = Stream.builder();
        for (Node n : getEachNode()) {
            String locationName = n.getId();
            Location location = getLocation(locationName);
            stream.accept(location);
        }
        return stream.build();
    }

    /**
     * Get a stream of all the road edges in the graph.
     *
     * @return a stream of all the road edges
     */
    public Stream<RoadEdge> getAllRoads() {
        Stream.Builder<RoadEdge> stream = Stream.builder();
        for (Edge e : getEachEdge()) {
            Road road = getAttribute(e.getId());
            stream.accept(RoadEdge.of(road, getLocation(e.getNode0().getId()), getLocation(e.getNode1().getId())));
        }
        return stream.build();
    }

    /**
     * Set the correct CSS style class on the node based on if it has a petrol station.
     *
     * @param location the location
     */
    protected void setPetrolStationStyle(Location location) {
        Node node = getNode(location.getName());
        if (location.hasPetrolStation()) {
            node.addAttribute("ui.class", "petrol");
        } else {
            node.setAttribute("ui.class", "");
        }
    }

    /**
     * Add a new edge to the graph representing the road.
     *
     * @param road the road to add
     * @param from the source location
     * @param to the destination location
     * @param <T> the type of the edge object
     * @param <R> the type of the road object
     * @return the created edge
     */
    public <T extends Edge, R extends Road> T addRoad(R road, Location from, Location to) {
        addAttribute(road.getName(), road);
        T edge = addEdge(road.getName(), from.getName(), to.getName(), true);
        edge.setAttribute("road", road);
        return edge;
    }

    /**
     * Put (add with override) a new edge to the graph representing the road.
     *
     * @param road the road to add
     * @param from the source location
     * @param to the destination location
     * @param <T> the type of the edge object
     * @param <R> the type of the road object
     * @return the created edge
     */
    public <T extends Edge, R extends Road> T putRoad(R road, Location from, Location to) {
        removeAttribute(road.getName());
        try {
            removeEdge(road.getName());
        } catch (ElementNotFoundException e) {
            logger.debug("Caught exception while putting road.", e);
        }
        return addRoad(road, from, to);
    }

    /**
     * Get stream of all the roads going from the first to the second location.
     * In the current implementation, should return the same
     * as {@link #getShortestRoadBetween(Location, Location)}.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @return a stream of all the roads {@code l1 -> l2}
     */
    public Stream<Road> getAllRoadsBetween(Location l1, Location l2) {
        Node n1 = getNode(l1.getName());
        if (n1 == null) {
            logger.debug("Could not find node \"{}\"", l1.getName());
            return null;
        }
        Node n2 = getNode(l2.getName());
        if (n2 == null) {
            logger.debug("Could not find node \"{}\"", l2.getName());
            return null;
        }
        return n1.getLeavingEdgeSet().stream().filter(edge -> edge.getNode1().equals(n2))
                .map(edge -> edge.getAttribute("road"));
    }

    /**
     * Only a single road is currently permitted between two nodes in practice. This method returns that
     * road, or null if there is none.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @return the shorted road between l1 and l2, or null if no such road exists
     */
    public Road getShortestRoadBetween(Location l1, Location l2) {
        return getAllRoadsBetween(l1, l2).min(Comparator.comparing(Road::getLength)).orElse(null);
    }

    /**
     * Removes all nodes going from l1 to l2.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @see #getAllRoadsBetween(Location, Location)
     */
    public void removeAllRoadsBetween(Location l1, Location l2) {
        removeRoads(getAllRoadsBetween(l1, l2).map(Road::getName).collect(Collectors.toList()));
    }

    /**
     * Get the road given by the name.
     *
     * @param name the name of the road
     * @return the road, null if a road like that doesn't exist
     */
    public Road getRoad(String name) {
        return getAttribute(name);
    }

    /**
     * Get the road edge given by the name of the road.
     *
     * @param name the name of the road
     * @return the road edge, null if a road like that doesn't exist
     */
    public RoadEdge getRoadEdge(String name) {
        Edge edge = getEdge(name);
        if (edge == null) {
            return null;
        }
        return RoadEdge.of(edge.getAttribute("road"), getAttribute(edge.getNode0().getId()),
                getAttribute(edge.getNode1().getId()));
    }

    /**
     * Remove the road given by the name. Also removes its edge sprite and edge.
     *
     * @param name the name of the road
     */
    public void removeRoad(String name) {
        try {
            removeEdge(name);
        } catch (NoSuchElementException e) {
            logger.debug("Caught exception while removing road.", e);
        }
        removeAttribute(name);
    }

    /**
     * Remove all the roads by their names.
     *
     * @param names the names of roads to remove
     * @see #removeRoad(String)
     */
    public void removeRoads(Iterable<? extends String> names) {
        names.forEach(this::removeRoad);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getAllLocations().toArray())
                .append(getAllRoads().toArray())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoadGraph)) {
            return false;
        }
        RoadGraph that = (RoadGraph) o;
        return new EqualsBuilder().append(getAllLocations().collect(Collectors.toSet()),
                that.getAllLocations().collect(Collectors.toSet()))
                .append(getAllRoads().collect(Collectors.toSet()), that.getAllRoads().collect(Collectors.toSet()))
                .isEquals();
    }

    /**
     * Represents a road along with the two locations between which it spans. Is not used internally
     * in the graph, serves as public API that is calculated based on the internal representation.
     */
    public static final class RoadEdge {

        private final Road road;
        private final Location from;
        private final Location to;

        /**
         * Private default constructor.
         *
         * @param road the road
         * @param from the from location
         * @param to the to location
         */
        private RoadEdge(Road road, Location from, Location to) {
            this.road = road;
            this.from = from;
            this.to = to;
        }

        /**
         * Build a road edge from the arguments.
         *
         * @param road the road
         * @param from the from location
         * @param to the to location
         * @return the built road edge
         */
        public static RoadEdge of(Road road, Location from, Location to) {
            return new RoadEdge(road, from, to);
        }

        /**
         * Get the road.
         *
         * @return the road
         */
        public Road getRoad() {
            return road;
        }

        /**
         * Get the from location.
         *
         * @return the from location
         */
        public Location getFrom() {
            return from;
        }

        /**
         * Get the to location.
         *
         * @return the to location
         */
        public Location getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RoadEdge)) {
                return false;
            }
            RoadEdge roadEdge = (RoadEdge) o;
            return new EqualsBuilder().append(getRoad(), roadEdge.getRoad()).append(getFrom(), roadEdge.getFrom())
                    .append(getTo(), roadEdge.getTo()).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getRoad()).append(getFrom())
                    .append(getTo()).toHashCode();
        }

        @Override
        public String toString() {
            return "RoadEdge{" + "road=" + road + ", from=" + from + ", to=" + to + '}';
        }
    }

}
