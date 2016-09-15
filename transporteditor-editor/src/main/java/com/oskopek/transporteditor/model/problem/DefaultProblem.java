/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

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

    public DefaultProblem(DefaultProblem defaultProblem) {
        // vehicles are immutable
        // packages are immutable
        this(defaultProblem.getName(), new RoadGraph(defaultProblem.getRoadGraph()),
                new HashMap<>(defaultProblem.vehicleMap), new HashMap<>(defaultProblem.packageMap));
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
    public Collection<Package> getAllPackages() {
        return packageMap.values();
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
                vehicleMap, that.vehicleMap)
                .append(packageMap, that.packageMap).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).append(getRoadGraph()).append(vehicleMap).append(
                packageMap).toHashCode();
    }
}
