package com.oskopek.transport.view.problem;

import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.VisualRoadGraph;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Implementation of a problem for the UI.
 */
public class VisualProblem implements Problem {

    private final String name;
    private final VisualRoadGraph roadGraph;
    private final Map<String, Vehicle> vehicleMap;
    private final Map<String, Package> packageMap;

    /**
     * Copy constructor.
     *
     * @param problem the problem to copy
     */
    public VisualProblem(Problem problem) {
        // vehicles are immutable
        // packages are immutable
        this(problem.getName(), problem.getVisualRoadGraph().copy(),
                new HashMap<>(problem.getVehicleMap()), new HashMap<>(problem.getPackageMap()));
    }

    /**
     * Default constructor.
     *
     * @param name the name
     * @param roadGraph the graph
     * @param vehicleMap the vehicle map
     * @param packageMap the package map
     */
    public VisualProblem(String name, VisualRoadGraph roadGraph, Map<String, Vehicle> vehicleMap,
            Map<String, Package> packageMap) {
        this.name = name;
        this.roadGraph = roadGraph;
        this.vehicleMap = vehicleMap;
        this.packageMap = packageMap;
    }

    @Override
    public VisualRoadGraph getVisualRoadGraph() {
        return roadGraph;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public VisualRoadGraph getRoadGraph() {
        return roadGraph;
    }

    @Override
    public Vehicle getVehicle(String name) {
        return vehicleMap.get(name);
    }

    @Override
    public Package getPackage(String name) {
        return packageMap.get(name);
    }

    @Override
    public Locatable getLocatable(String name) {
        Vehicle vehicle = getVehicle(name);
        if (vehicle != null) {
            return vehicle;
        }
        Package aPackage = getPackage(name);
        if (aPackage != null) {
            return aPackage;
        }
        Location location = roadGraph.getLocation(name);
        if (location != null) {
            return location;
        }
        return null;
    }

    @Override
    public ActionObject getActionObject(String name) {
        Locatable locatable = getLocatable(name);
        if (locatable != null) {
            return locatable;
        }
        Road aRoad = roadGraph.getRoad(name);
        if (aRoad != null) {
            return aRoad;
        }
        return null;
    }

    @Override
    public Collection<Vehicle> getAllVehicles() {
        return vehicleMap.values();
    }

    @Override
    public Map<String, Vehicle> getVehicleMap() {
        return vehicleMap;
    }

    @Override
    public Collection<Package> getAllPackages() {
        return packageMap.values();
    }

    @Override
    public Map<String, Package> getPackageMap() {
        return packageMap;
    }

    @Override
    public VisualProblem putVehicle(String name, Vehicle vehicle) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(vehicleMap);
        newVehicleMap.put(name, vehicle);
        return new VisualProblem(this.name, roadGraph, newVehicleMap, packageMap);
    }

    /**
     * Put all vehicles in the stream.
     *
     * @param vehicles the vehicles to put, keys are their names
     * @return the updated problem
     * @see #putVehicle(String, Vehicle)
     */
    public VisualProblem putAllVehicles(Stream<Vehicle> vehicles) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(vehicleMap);
        vehicles.forEach(v -> newVehicleMap.put(v.getName(), v));
        return new VisualProblem(name, roadGraph, newVehicleMap, packageMap);
    }

    @Override
    public VisualProblem putPackage(String name, Package pkg) {
        Map<String, Package> newPackageMap = new HashMap<>(packageMap);
        newPackageMap.put(name, pkg);
        return new VisualProblem(this.name, roadGraph, vehicleMap, newPackageMap);
    }

    @Override
    public VisualProblem changeActionObjectName(ActionObject actionObject, String newName) {
        if (actionObject.getName().equals(newName)) {
            return this;
        } else {
            throw new UnsupportedOperationException("Cannot change action object name.");
        }
    }

    @Override
    public VisualProblem removeVehicle(String name) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(vehicleMap);
        newVehicleMap.remove(name);
        return new VisualProblem(this.name, roadGraph, newVehicleMap, packageMap);
    }

    @Override
    public VisualProblem removePackage(String name) {
        Map<String, Package> newPackageMap = new HashMap<>(packageMap);
        newPackageMap.remove(name);
        return new VisualProblem(this.name, roadGraph, vehicleMap, newPackageMap);
    }

    @Override
    public VisualProblem removeLocation(String name) { // TODO: Should this be immutable too? Yes!
        roadGraph.removeLocation(roadGraph.getLocation(name));
        return this;
    }

    @Override
    public VisualProblem putLocation(String name, Location location) { // TODO: Should this be immutable too? Yes!
        roadGraph.moveLocation(name, location.getxCoordinate(), location.getyCoordinate());
        roadGraph.setPetrolStation(name, location.getPetrolStation());
        return this;
    }

    @Override
    public VisualProblem putName(String newName) {
        return new VisualProblem(newName, roadGraph, vehicleMap, packageMap);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(roadGraph).append(vehicleMap).append(
                packageMap).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VisualProblem)) {
            return false;
        }
        VisualProblem that = (VisualProblem) o;
        return new EqualsBuilder().append(name, that.name).append(roadGraph, that.roadGraph).append(
                vehicleMap, that.vehicleMap).append(packageMap, that.packageMap).isEquals();
    }
}
