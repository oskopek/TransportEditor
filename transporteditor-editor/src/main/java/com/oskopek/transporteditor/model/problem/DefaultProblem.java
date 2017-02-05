package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultProblem implements Problem {

    private final String name;
    private final RoadGraph roadGraph;
    private final Map<String, Vehicle> vehicleMap;
    private final Map<String, Package> packageMap;

    public DefaultProblem(Problem problem) {
        // vehicles are immutable
        // packages are immutable
        this(problem.getName(), new RoadGraph(problem.getRoadGraph()),
                new HashMap<>(problem.getVehicleMap()), new HashMap<>(problem.getPackageMap()));
    }

    public DefaultProblem(String name, RoadGraph roadGraph, Map<String, Vehicle> vehicleMap,
            Map<String, Package> packageMap) {
        this.name = name;
        this.roadGraph = roadGraph;
        this.vehicleMap = vehicleMap;
        this.packageMap = packageMap;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RoadGraph getRoadGraph() {
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
        Location location = getRoadGraph().getLocation(name);
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
    public Problem putVehicle(String name, Vehicle vehicle) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(getVehicleMap());
        newVehicleMap.put(name, vehicle);
        return new DefaultProblem(getName(), getRoadGraph(), newVehicleMap, getPackageMap());
    }

    @Override
    public Problem putPackage(String name, Package pkg) {
        Map<String, Package> newPackageMap = new HashMap<>(getPackageMap());
        newPackageMap.put(name, pkg);
        return new DefaultProblem(getName(), getRoadGraph(), getVehicleMap(), newPackageMap);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).append(getRoadGraph()).append(vehicleMap).append(
                packageMap).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DefaultProblem)) {
            return false;
        }

        DefaultProblem that = (DefaultProblem) o;

        return new EqualsBuilder().append(getName(), that.getName()).append(getRoadGraph(), that.getRoadGraph()).append(
                getVehicleMap(), that.getVehicleMap()).append(getPackageMap(), that.getPackageMap()).isEquals();
    }
}
