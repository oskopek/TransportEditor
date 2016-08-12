/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;

import java.util.List;

public interface Problem {

    RoadGraph getRoadGraph();

    void setRoadGraph(RoadGraph roadGraph);

    ObjectProperty<RoadGraph> roadGraphProperty();

    List<Vehicle> getVehicleList();

    void setVehicleList(List<Vehicle> vehicleList);

    ListProperty<Vehicle> vehicleListProperty();

    List<Package> getPackageList();

    void setPackageList(List<Package> packageList);

    ListProperty<Package> packageListProperty();

}
