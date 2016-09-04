/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import java.util.List;

public interface Problem {

    RoadGraph getRoadGraph();

    void setRoadGraph(RoadGraph roadGraph);

    List<Vehicle> getVehicleList();

    void setVehicleList(List<Vehicle> vehicleList);

    List<Package> getPackageList();

    void setPackageList(List<Package> packageList);

}
