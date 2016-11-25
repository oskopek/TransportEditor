package com.oskopek.transporteditor.model.problem;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.view.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
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

    public <T extends Node> T addLocation(Location location) {
        addAttribute(location.getName(), location);
        T node = addNode(location.getName());
        node.addAttribute("ui.label", location.getName());
        return node;
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
        removeEdge(road.getName());
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

    public Road getRoadBetween(Location l1, Location l2) {
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
        Edge e = n1.getEdgeToward(n2);
        if (e == null) {
            logger.debug("Could not find edge between given nodes \"{}\" and \"{}\".", l1.getName(), l2.getName());
            return null;
        }
        return e.getAttribute("road");
    }

    public Road getRoad(String name) {
        return getAttribute(name);
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
