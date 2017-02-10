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

    Problem putLocation(String name, Location location);

    Problem changeActionObjectName(ActionObject actionObject, String newName);

    default Problem changeVehicle(Vehicle oldVehicle, Vehicle newVehicle) {
        String newName = newVehicle.getName();
        return changeActionObjectName(oldVehicle, newName).putVehicle(newName, newVehicle);
    }

    default Problem changePackage(Package oldPackage, Package newPackage) {
        String newName = newPackage.getName();
        return changeActionObjectName(oldPackage, newName).putPackage(newName, newPackage);
    }

    default Problem changeLocation(Location oldLocation, Location newLocation) {
        String newName = newLocation.getName();
        return changeActionObjectName(oldLocation, newName).putLocation(newName, newLocation);
    }

}
