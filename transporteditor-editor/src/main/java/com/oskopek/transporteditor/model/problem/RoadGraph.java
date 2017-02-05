package com.oskopek.transporteditor.model.problem;

import javaslang.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.view.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper interface around a GraphStream graph type.
 */
public class RoadGraph extends MultiGraph implements Graph {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RoadGraph(RoadGraph graph) {
        this(graph.getId());
        graph.getAllLocations().forEach(this::addLocation);
        graph.getAllRoads().forEach(e -> this.addRoad(e.getRoad(), e.getFrom(), e.getTo()));
    }

    public RoadGraph(String id) {
        super(id);

        addDefaultStyling();
    }

    private RoadGraph(String id, boolean strictChecking, boolean autoCreate) {
        super(id, strictChecking, autoCreate);
    }

    private RoadGraph(String id, boolean strictChecking, boolean autoCreate, int initialNodeCapacity,
            int initialEdgeCapacity) {
        super(id, strictChecking, autoCreate, initialNodeCapacity, initialEdgeCapacity);
    }

    private void addDefaultStyling() {
        String style;
        try {
            style = String.join("\n",
                    (List<String>) IOUtils.readLines(getClass().getResourceAsStream("stylesheet.css"), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load graph stylesheet.", e);
        }
        this.addAttribute("ui.stylesheet", style);
        //        this.addAttribute("ui.quality");
        this.addAttribute("ui.antialias");
    }

    @Deprecated
    public Optional<Point3> calculateCentroid() {
        return getNodeSet().stream().map(Toolkit::nodePosition).map(c -> Tuple.of(c[0], c[1], c[2]))
                .reduce((t1, t2) -> Tuple.of(t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3))
                .map(t -> Tuple.of(t._1/nodeCount, t._2/nodeCount, t._3/nodeCount))
                .map(t -> new Point3(t._1, t._2, t._3));
    }

    public <T extends Node> T addLocation(Location location) {
        addAttribute(location.getName(), location);
        T node = addNode(location.getName());
        node.addAttribute("ui.label", location.getName());
        node.addAttribute("xyz", new Point3(location.getxCoordinate(), location.getyCoordinate(), 0));
        return node;
    }

    public void removeLocation(Location location) {
        removeRoads(getAllRoads().filter(re -> re.getFrom().equals(location) || re.getTo().equals(location))
                .map(r -> r.getRoad().getName()).collect(Collectors.toList()));
        removeNode(location.getName());
        removeAttribute(location.getName());
    }

    public void removeLocations(Collection<? extends Location> locations) {
        locations.forEach(this::removeLocation);
    }

    public Location moveLocation(String name, int newX, int newY) {
        Location original = getAttribute(name);
        Location newLocation = new Location(original.getName(), newX, newY);
        setAttribute(newLocation.getName(), newLocation);
        return original;
    }

    public Location getLocation(String name) {
        return getAttribute(name);
    }

    public Stream<Location> getAllLocations() {
        Stream.Builder<Location> stream = Stream.builder();
        for (Node n : getEachNode()) {
            String locationName = n.getId();
            Location location = getLocation(locationName);
            stream.accept(location);
        }
        return stream.build();
    }

    public Stream<RoadEdge> getAllRoads() {
        Stream.Builder<RoadEdge> stream = Stream.builder();
        for (Edge e : getEachEdge()) {
            Road road = getAttribute(e.getId());
            stream.accept(RoadEdge.of(road, getLocation(e.getNode0().getId()), getLocation(e.getNode1().getId())));
        }
        return stream.build();
    }

    public <T extends Edge, R extends Road> T addRoad(R road, Location from, Location to) {
        addAttribute(road.getName(), road);
        T edge = addEdge(road.getName(), from.getName(), to.getName(), true);
        edge.setAttribute("road", road);
        return edge;
    }

    public <T extends Edge, R extends Road> T putRoad(R road, Location from, Location to) {
        removeAttribute(road.getName());
        try {
            removeEdge(road.getName());
        } catch (ElementNotFoundException e) {
            logger.debug("Caught exception while putting road.", e);
        }
        addAttribute(road.getName(), road);
        T edge = addEdge(road.getName(), from.getName(), to.getName(), true);
        edge.setAttribute("road", road);
        return edge;
    }

    public boolean hasPetrolStation(Location location) {
        return hasAttribute(location.getName() + "-station");
    }

    public void setPetrolStation(Location location) {
        setAttribute(location.getName() + "-station");
    }

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
     * Only a single road is permitted between two nodes in practice, but our model is a bit more flexible.
     *
     * @param l1 the from location
     * @param l2 the to location
     * @return the shorted road between l1 and l2, or null if no such road exists
     */
    public Road getShortestRoadBetween(Location l1, Location l2) {
        return getAllRoadsBetween(l1, l2).min(Comparator.comparing(Road::getLength)).orElse(null);
    }

    public void removeAllRoadsBetween(Location l1, Location l2) {
        removeRoads(getAllRoadsBetween(l1, l2).map(Road::getName).collect(Collectors.toList()));
    }

    public Road getRoad(String name) {
        return getAttribute(name);
    }

    public void removeRoad(String name) {
        try {
            removeEdge(name);
        } catch (NoSuchElementException e) {
            logger.debug("Caught exception while removing road.", e);
        }
        removeAttribute(name);
    }

    public void removeRoads(Collection<? extends String> names) {
        names.forEach(this::removeRoad);
    }

    @Override
    public Viewer display() {
        return display(true);
    }

    @Override
    public Viewer display(boolean autoLayout) {
        Viewer viewer = new Viewer(this, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        if (autoLayout) {
            Layout layout = Layouts.newLayoutAlgorithm();
            viewer.enableAutoLayout(layout);
        }
        return viewer;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getAllLocations().toArray())
                .append(getAllRoads().toArray())
                .toHashCode();
    }

    public static final class RoadEdge {

        private final Road road;
        private final Location from;
        private final Location to;

        private RoadEdge(Road road, Location from, Location to) {
            this.road = road;
            this.from = from;
            this.to = to;
        }

        public static RoadEdge of(Road road, Location from, Location to) {
            return new RoadEdge(road, from, to);
        }

        public Road getRoad() {
            return road;
        }

        public Location getFrom() {
            return from;
        }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoadGraph)) {
            return false;
        }
        RoadGraph that = (RoadGraph) o;
        return new EqualsBuilder()
                .append(getAllLocations().toArray(), that.getAllLocations().toArray())
                .append(getAllRoads().toArray(), that.getAllRoads().toArray())
                .isEquals();
    }

}
