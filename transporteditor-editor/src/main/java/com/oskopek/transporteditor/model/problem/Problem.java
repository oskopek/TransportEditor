/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import java.util.List;

public interface Problem extends Cloneable {

    RoadGraph getRoadGraph();

    Vehicle getVehicle(String name);

    Package getPackage(String name);

    Locatable getLocatable(String name);

    ActionObject getActionObject(String name);

    List<Vehicle> getAllVehicles();

    List<Package> getAllPackages();



}
