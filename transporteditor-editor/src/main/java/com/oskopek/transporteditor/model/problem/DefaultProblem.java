/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class DefaultProblem implements Problem {

    private final RoadGraph roadGraph;
    private final List<Vehicle> vehicleList;
    private final List<Package> packageList;

    public DefaultProblem(DefaultProblem defaultProblem) {
        this.roadGraph = new RoadGraph(defaultProblem.getRoadGraph());
        this.vehicleList = defaultProblem.getVehicleList(); // vehicles are immutable
        this.packageList = defaultProblem.getPackageList(); // packages are immutable
    }

    public DefaultProblem(RoadGraph roadGraph, List<Vehicle> vehicleList, List<Package> packageList) {
        this.roadGraph = roadGraph;
        this.vehicleList = vehicleList;
        this.packageList = packageList;
    }

    @Override
    public RoadGraph getRoadGraph() {
        return roadGraph;
    }

    @Override
    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    @Override
    public List<Package> getPackageList() {
        return packageList;
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

        return new EqualsBuilder().append(getRoadGraph(), that.getRoadGraph()).append(getVehicleList(),
                that.getVehicleList()).append(getPackageList(), that.getPackageList()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getRoadGraph()).append(getVehicleList()).append(getPackageList())
                .toHashCode();
    }
}
