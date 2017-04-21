package com.oskopek.transport.view.problem;

import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.graph.VisualRoadGraph;
import com.oskopek.transport.model.state.PlanState;
import com.oskopek.transport.persistence.IOUtils;
import com.oskopek.transport.view.SpriteBuilder;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Node;
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

/**
 * {@inheritDoc}
 * <p>
 * Displays edge and vehicle/package properties using {@link Sprite}s with a {@link SpriteManager},
 * because GraphStream doesn't have a notion of clicking on an edge. These sprites should always be
 * attached to a node or edge.
 * The state mutator methods automatically cause graph redraws.
 */
public final class DefaultVisualRoadGraph extends DefaultRoadGraph implements VisualRoadGraph {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private transient SpriteManager spriteManager;
    private transient double packageDegreeDelta;
    private transient double vehicleDegreeDelta;
    private transient double vehicleRadius;
    private transient double packageRadius;

    /**
     * Copy constructor.
     *
     * @param graph the graph to copy
     */
    public DefaultVisualRoadGraph(RoadGraph graph) {
        super(graph.getId());
        setDefaultStyling();
        graph.getAllLocations().forEach(this::addLocation);
        graph.getAllRoads().forEach(e -> this.addRoad(e.getRoad(), e.getFrom(), e.getTo()));
    }

    /**
     * Deserialization helper method.
     *
     * @return a new visual graph with correctly initialized transient fields
     */
    protected Object readResolve() {
        setDefaultStyling();
        return this;
    }

    @Override
    public DefaultVisualRoadGraph copy() {
        return new DefaultVisualRoadGraph(this);
    }

    /**
     * Constructs a new empty visual graph.
     *
     * @param id the name of the graph
     */
    public DefaultVisualRoadGraph(String id) {
        super(id);
        setDefaultStyling();
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
    public <T extends Node> T addLocation(Location location) {
        T node = super.addLocation(location);
        node.addAttribute("xyz", new Point3(location.getxCoordinate(), location.getyCoordinate(), 0));
        return node;
    }

    @Override
    public <T extends Edge, R extends Road> T addRoad(R road, Location from, Location to) {
        T returned = super.addRoad(road, from, to);
        addEdgeSprite(returned);
        return returned;
    }

    @Override
    public <T extends Edge, R extends Road> T putRoad(R road, Location from, Location to) {
        try {
            removeSprite(road.getName());
        } catch (ElementNotFoundException e) {
            logger.debug("Caught exception while putting road.", e);
        }
        return super.putRoad(road, from, to);
    }

    @Override
    public void removeRoad(String name) {
        try {
            removeSprite(name);
        } catch (NoSuchElementException e) {
            logger.debug("Caught exception while removing road.", e);
        }
        super.removeRoad(name);
    }

    /**
     * Set the default CSS stylesheet and various rendering properties.
     */
    public synchronized void setDefaultStyling() {
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
        createNewSpriteManager();
    }

    /**
     * Create a new sprite manager for the current graph (reset).
     */
    private synchronized void createNewSpriteManager() {
        spriteManager = new SpriteManager(this);
    }

    /**
     * Removes all sprites from the sprite manager and detaches it + resets graph styling.
     */
    private synchronized void removeAllActionObjectSprites() {
        packageDegreeDelta = 0d;
        vehicleDegreeDelta = 0d;
        vehicleRadius = 25d;
        packageRadius = 25d;
        List<String> spriteNames = new ArrayList<>(spriteManager.getSpriteCount());
        spriteManager.sprites().forEach(s -> spriteNames.add(s.getId()));
        spriteNames.forEach(s -> spriteManager.removeSprite(s));
        spriteManager.detach();
        createNewSpriteManager();
    }

    /**
     * Set the correct CSS style class on the node based on if it has a petrol station.
     *
     * @param location the location
     */
    private void setPetrolStationStyle(Location location) {
        Node node = getNode(location.getName());
        if (location.hasPetrolStation()) {
            node.addAttribute("ui.class", "petrol");
        } else {
            node.setAttribute("ui.class", "");
        }
    }

    @Override
    public synchronized void redrawActionObjectSprites(Problem problem) {
        removeAllActionObjectSprites();
        getEdgeSet().stream().filter(e -> !spriteManager.hasSprite("sprite-" + e.getId())).forEach(this::addEdgeSprite);
        problem.getAllVehicles().stream().sorted(Comparator.comparing(DefaultActionObject::getName))
                .forEach(v -> addVehicleSprite(v, v.getLocation()));
        problem.getAllPackages().stream().sorted(Comparator.comparing(DefaultActionObject::getName))
                .forEach(p -> addPackageSprite(p, p.getLocation()));
    }

    @Override
    public synchronized void redrawPackagesVehiclesFromPlanState(PlanState state) {
        removeAllActionObjectSprites();
        getEdgeSet().stream().filter(e -> !spriteManager.hasSprite("sprite-" + e.getId())).forEach(this::addEdgeSprite);

        Collection<Package> allPackages = state.getAllPackages();
        Map<Package, Location> packagesAtNodes = new HashMap<>(allPackages.size());
        Map<Package, Road> packagesAtEdges = new HashMap<>(allPackages.size());

        // draw vehicles
        state.getAllVehicles().stream().sorted(Comparator.comparing(DefaultActionObject::getName)).forEach(v -> {
            String vehicleLocationName = v.getLocation().getName();
            boolean isAtLocation = hasAttribute(vehicleLocationName, Location.class);

            Location vehicleLocation = null;
            Road vehicleRoad = null;
            if (isAtLocation) {
                vehicleLocation = getLocation(vehicleLocationName);
            } else {
                vehicleRoad = getRoad(vehicleLocationName);
            }

            if (isAtLocation) {
                addVehicleSprite(v, vehicleLocation);
            } else { // at edge
                addVehicleSprite(v, vehicleRoad, 0.5d);
            }

            for (Package pkg : v.getPackageList()) {
                if (isAtLocation) {
                    packagesAtNodes.put(pkg, vehicleLocation);
                } else { // at edge
                    packagesAtEdges.put(pkg, vehicleRoad);
                }
            }
        });

        // draw packages
        allPackages.stream().sorted(Comparator.comparing(DefaultActionObject::getName))
                .forEach(p -> {
                    if (p.getLocation() != null) {
                        addPackageSprite(p, p.getLocation());
                    } else if (packagesAtNodes.containsKey(p)) {
                        addPackageSprite(p, packagesAtNodes.get(p));
                    } else if (packagesAtEdges.containsKey(p)) {
                        addPackageSprite(p, packagesAtEdges.get(p), 0.5d);
                    }
                });
    }

    /**
     * Adds a sprite of the action object name to the sprite manager.
     *
     * @param name the name of the action object who's sprite to add
     * @return a sprite builder of the sprite
     */
    private SpriteBuilder<Sprite> addSprite(String name) {
        return new SpriteBuilder<>(spriteManager, "sprite-" + name, Sprite.class);
    }

    /**
     * Removes a sprite of the action object name from the sprite manager.
     *
     * @param name the name of the action object who's sprite to remove
     */
    private void removeSprite(String name) {
        spriteManager.removeSprite("sprite-" + name);
    }

    /**
     * Adds an edge sprite to the graph, attached to the edge halfway along its run.
     *
     * @param edge the edge to add sprite to
     */
    private void addEdgeSprite(Edge edge) {
        addSprite(edge.getId()).attachTo(edge).setPosition(0.5d, 0, 0);
    }

    /**
     * Adds a package sprite to the graph, returning a sprite builder. <strong>Needs to be finished
     * by attaching to a location/road.</strong>
     *
     * @param pkg the package
     * @return the sprite builder for this sprite
     */
    private SpriteBuilder addPackageSprite(Package pkg) {
        return addSprite(pkg.getName()).setClass("package");
    }

    @Override
    public void addPackageSprite(Package pkg, Location location) {
        packageDegreeDelta += 25d;
        if (packageDegreeDelta > 180d) {
            packageRadius += 12d;
            packageDegreeDelta %= 180d;
        }

        SpriteBuilder sprite = addPackageSprite(pkg).setPosition(packageRadius, 180d + packageDegreeDelta);
        if (location != null) {
            sprite.attachTo((Node) getNode(location.getName()));
        }
    }

    @Override
    public void addPackageSprite(Package pkg, Road road, double percentage) {
        packageDegreeDelta += 25d;
        if (packageDegreeDelta > 180d) {
            packageRadius += 12d;
            packageDegreeDelta %= 180d;
        }

        SpriteBuilder sprite = addPackageSprite(pkg).setPosition(percentage, packageRadius, 180d + packageDegreeDelta);
        if (road != null) {
            sprite.attachTo((Edge) getEdge(road.getName()));
        }
    }

    /**
     * Adds a vehicle sprite to the graph, returning a sprite builder. <strong>Needs to be finished
     * by attaching to a location/road.</strong>
     *
     * @param vehicle the vehicle
     * @return the sprite builder for this sprite
     */
    private SpriteBuilder addVehicleSprite(Vehicle vehicle) {
        return addSprite(vehicle.getName()).setClass("vehicle");
    }

    @Override
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

    @Override
    public void addVehicleSprite(Vehicle vehicle, Road road, double percentage) {
        vehicleDegreeDelta += 25d;
        if (vehicleDegreeDelta > 180d) {
            vehicleRadius += 12d;
            vehicleDegreeDelta %= 180d;
        }

        SpriteBuilder sprite = addVehicleSprite(vehicle).setPosition(percentage, vehicleRadius, vehicleDegreeDelta);
        if (road != null) {
            sprite.attachTo((Edge) getEdge(road.getName()));
        }
    }

}
