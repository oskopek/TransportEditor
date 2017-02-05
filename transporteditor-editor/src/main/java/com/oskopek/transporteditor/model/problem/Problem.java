package com.oskopek.transporteditor.model.problem;

import java.util.Collection;
import java.util.Map;

public interface Problem {

    String getName();

    RoadGraph getRoadGraph();

    Vehicle getVehicle(String name);

    Package getPackage(String name);

    Locatable getLocatable(String name);

    ActionObject getActionObject(String name);

    Collection<Vehicle> getAllVehicles();

    Map<String, Vehicle> getVehicleMap();

    Collection<Package> getAllPackages();

    Map<String, Package> getPackageMap();

    Problem putVehicle(String name, Vehicle vehicle);

    Problem putPackage(String name, Package pkg);

}
