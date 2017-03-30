package com.oskopek.transport.model.problem.graph;

import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.state.PlanState;

/**
 * Road graph extension, used for UI purposes.
 */
public interface VisualRoadGraph extends RoadGraph {

    @Override
    VisualRoadGraph copy();

    /**
     * Remove and add all sprites.
     *
     * @param problem the problem from which to get the action objects
     */
    void redrawActionObjectSprites(Problem problem);

    /**
     * Remove and add all sprites, taking care to draw vehicles and packages at their current state location
     * (edges, ...).
     *
     * @param state the current problem state from which to get the action objects
     */
    void redrawPackagesVehiclesFromPlanState(PlanState state);

    /**
     * Adds a package sprite to the graph, attached to the given location.
     *
     * @param pkg the package
     * @param location the location
     */
    void addPackageSprite(Package pkg, Location location);

    /**
     * Adds a package sprite to the graph, attached to the given road at a percentage of it's length.
     *
     * @param pkg the package
     * @param road the road
     * @param percentage the percentage from source to destination on which to add sprite
     */
    void addPackageSprite(Package pkg, Road road, double percentage);

    /**
     * Adds a vehicle sprite to the graph, attached to the given location.
     *
     * @param vehicle the vehicle
     * @param location the location
     */
    void addVehicleSprite(Vehicle vehicle, Location location);

    /**
     * Adds a vehicle sprite to the graph, attached to the given road at a percentage of it's length.
     *
     * @param vehicle the vehicle
     * @param road the road
     * @param percentage the percentage from source to destination on which to add sprite
     */
    void addVehicleSprite(Vehicle vehicle, Road road, double percentage);

}
