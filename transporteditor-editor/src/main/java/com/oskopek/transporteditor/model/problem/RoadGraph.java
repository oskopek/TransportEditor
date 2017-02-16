package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.view.SpriteBuilder;
import javaslang.Tuple;
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
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
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
public class RoadGraph extends MultiGraph implements Graph { // TODO: Refactor GUI out of this

    private transient SpriteManager spriteManager;
    private transient double packageDegreeDelta;
    private transient double vehicleDegreeDelta;
    private transient double vehicleRadius;
    private transient double packageRadius;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RoadGraph(RoadGraph graph) {
        this(graph.getId());
        graph.getAllLocations().forEach(this::addLocation);
        graph.getAllRoads().forEach(e -> this.addRoad(e.getRoad(), e.getFrom(), e.getTo()));
    }

    public RoadGraph(String id) {
        super(id);
        setDefaultStyling();
    }

    public void setDefaultStyling() {
        String style;
        try {
            style = String.join("\n", IOUtils.concatReadAllLines(getClass().getResourceAsStream("stylesheet.css")));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load graph stylesheet.", e);
        }
        this.setAttribute("ui.stylesheet", style);
        // this.setAttribute("ui.quality");
        this.setAttribute("ui.antialias");
        getAllLocations().forEach(this::setPetrolStationStyle);
        spriteManager = new SpriteManager(this);
    }

    @Deprecated
    public Optional<Point3> calculateCentroid() {
        return getNodeSet().stream().map(Toolkit::nodePosition).map(c -> Tuple.of(c[0], c[1], c[2]))
                .reduce((t1, t2) -> Tuple.of(t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3))
                .map(t -> Tuple.of(t._1 / nodeCount, t._2 / nodeCount, t._3 / nodeCount))
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

    public void removeLocations(Iterable<? extends Location> locations) {
        locations.forEach(this::removeLocation);
    }

    public Location moveLocation(String name, int newX, int newY) {
        Location original = getAttribute(name);
        Location newLocation = new Location(original.getName(), newX, newY);
        setAttribute(newLocation.getName(), newLocation);
        return original;
    }

    public Location setPetrolStation(String locationName, boolean hasPetrolStation) {
        Location original = getAttribute(locationName);
        Location newLocation = original.updateHasPetrolStation(hasPetrolStation);
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

    private void removeAllActionObjectSprites() {
        packageDegreeDelta = 0d;
        vehicleDegreeDelta = 0d;
        vehicleRadius = 25d;
        packageRadius = 25d;
        List<String> spriteNames = new ArrayList<>(spriteManager.getSpriteCount());
        spriteManager.sprites().forEach(s -> spriteNames.add(s.getId()));
        spriteNames.forEach(s -> spriteManager.removeSprite(s));
        spriteManager.detach();
        setDefaultStyling(); // TODO: Hack
    }

    public void redrawActionObjectSprites(Problem problem) {
        removeAllActionObjectSprites();
        getEdgeSet().stream().filter(e -> !spriteManager.hasSprite("sprite-" + e.getId())).forEach(this::addEdgeSprite);
        problem.getAllPackages().forEach(p -> addPackageSprite(p, p.getLocation()));
        problem.getAllVehicles().forEach(v -> addVehicleSprite(v, v.getLocation()));
    }

    private SpriteBuilder<Sprite> addSprite(String name) {
        return new SpriteBuilder<>(spriteManager, "sprite-" + name, Sprite.class);
    }

    private void removeSprite(String name) {
        spriteManager.removeSprite("sprite-" + name);
    }

    private void addEdgeSprite(Edge edge) {
        addSprite(edge.getId()).attachTo(edge).setPosition(0.5d);
    }

    private SpriteBuilder addPackageSprite(Package pkg) {
        return addSprite(pkg.getName()).setClass("package");
    }

    public void addPackageSprite(Package pkg, Location location) {
        packageDegreeDelta += 25d;
        if (packageDegreeDelta > 180d) {
            packageRadius += 12d;
            packageDegreeDelta %= 180d;
        }

        SpriteBuilder sprite = addPackageSprite(pkg).setPosition(packageRadius,
                180d + packageDegreeDelta);
        if (location != null) {
            sprite.attachTo((Node) getNode(location.getName()));
        }
    }

    public void addPackageSprite(Package pkg, Road road, double percentage) {
        SpriteBuilder sprite = addPackageSprite(pkg).setPosition(percentage);
        if (road != null) {
            sprite.attachTo((Edge) getEdge(road.getName()));
        }
    }

    private SpriteBuilder addVehicleSprite(Vehicle vehicle) {
        return addSprite(vehicle.getName()).setClass("vehicle");
    }

    public void addVehicleSprite(Vehicle vehicle, Location location) {
        vehicleDegreeDelta += 25d;
        if (vehicleDegreeDelta > 180d) {
            vehicleRadius += 12d;
            vehicleDegreeDelta %= 180d;
        }
        SpriteBuilder sprite = addVehicleSprite(vehicle).setPosition(vehicleRadius, vehicleDegreeDelta);
        if (location != null) {
            sprite.attachTo((Node) getNode(location.getName()));
        }
    }

    public void addVehicleSprite(Vehicle vehicle, Road road, double percentage) {
        SpriteBuilder sprite = addVehicleSprite(vehicle).setPosition(percentage);
        if (road != null) {
            sprite.attachTo((Edge) getEdge(road.getName()));
        }
    }

    private void setPetrolStationStyle(Location location) {
        Node node = getNode(location.getName());
        if (location.hasPetrolStation()) {
            node.addAttribute("ui.class", "petrol");
        } else {
            node.setAttribute("ui.class", "");
        }
    }

    public <T extends Edge, R extends Road> T addRoad(R road, Location from, Location to) {
        addAttribute(road.getName(), road);
        T edge = addEdge(road.getName(), from.getName(), to.getName(), true);
        addEdgeSprite(edge);
        edge.setAttribute("road", road);
        return edge;
    }

    public <T extends Edge, R extends Road> T putRoad(R road, Location from, Location to) {
        removeAttribute(road.getName());
        try {
            removeSprite(road.getName());
            removeEdge(road.getName());
        } catch (ElementNotFoundException e) {
            logger.debug("Caught exception while putting road.", e);
        }
        return addRoad(road, from, to);
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

    public RoadEdge getRoadEdge(String name) {
        Edge edge = getEdge(name);
        if (edge == null) {
            return null;
        }
        return RoadEdge.of(edge.getAttribute("road"), getAttribute(edge.getNode0().getId()),
                getAttribute(edge.getNode1().getId()));
    }

    public void removeRoad(String name) {
        try {
            removeSprite(name);
            removeEdge(name);
        } catch (NoSuchElementException e) {
            logger.debug("Caught exception while removing road.", e);
        }
        removeAttribute(name);
    }

    public void removeRoads(Iterable<? extends String> names) {
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

}
