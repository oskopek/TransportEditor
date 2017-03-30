package com.oskopek.transporteditor.model.problem;

import java.util.Collection;
import java.util.Map;

/**
 * Transport domain's problem instance. Contains a lot of immutable data and mutator methods.
 * There are un-documented constraints on the behavior of some of the methods (see the Transport domain PDDL files
 * for more details).
 */
public interface Problem {

    /**
     * Get the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Get the road graph.
     *
     * @return the road graph
     */
    RoadGraph getRoadGraph();

    /**
     * Get the vehicle.
     *
     * @param name the name of the vehicle
     * @return the vehicle
     */
    Vehicle getVehicle(String name);

    /**
     * Get the package.
     *
     * @param name the name of the package
     * @return the package
     */
    Package getPackage(String name);

    /**
     * Get the locatable.
     *
     * @param name the name of the locatable
     * @return the locatable
     */
    Locatable getLocatable(String name);

    /**
     * Get the action object.
     *
     * @param name the name of the action object
     * @return the action object
     */
    ActionObject getActionObject(String name);

    /**
     * Get all the vehicles.
     *
     * @return the vehicles
     */
    Collection<Vehicle> getAllVehicles();

    /**
     * Get a map of all the vehicles.
     *
     * @return the vehicles, mapped from their names
     */
    Map<String, Vehicle> getVehicleMap();

    /**
     * Get all the packages.
     *
     * @return the packages
     */
    Collection<Package> getAllPackages();

    /**
     * Get a map of all the packages.
     *
     * @return the packages, mapped from their names
     */
    Map<String, Package> getPackageMap();

    /**
     * Update the name of the problem, creating a new problem.
     *
     * @param newName the new name
     * @return the updated problem
     */
    Problem putName(String newName);

    /**
     * Add or set a vehicle under the given name into the problem, creating a new problem.
     *
     * @param name the name
     * @param vehicle the vehicle
     * @return the updated problem
     */
    Problem putVehicle(String name, Vehicle vehicle);

    /**
     * Add or set a package under the given name into the problem, creating a new problem.
     *
     * @param name the name
     * @param pkg the package
     * @return the updated problem
     */
    Problem putPackage(String name, Package pkg);

    /**
     * Add or set a location under the given name into the problem, creating a new problem.
     *
     * @param name the name
     * @param location the location
     * @return the updated problem
     */
    Problem putLocation(String name, Location location);

    /**
     * Remove a vehicle under the given name from the problem, creating a new problem.
     *
     * @param name the name
     * @return the updated problem
     */
    Problem removeVehicle(String name);

    /**
     * Remove a package under the given name from the problem, creating a new problem.
     *
     * @param name the name
     * @return the updated problem
     */
    Problem removePackage(String name);

    /**
     * Remove a location under the given name from the problem, creating a new problem.
     *
     * @param name the name
     * @return the updated problem
     */
    Problem removeLocation(String name);

    /**
     * Exchanges the old action object's name for a new one, creating a new problem.
     *
     * @param actionObject the action object to change
     * @param newName the new name
     * @return the updated problem
     */
    Problem changeActionObjectName(ActionObject actionObject, String newName);

    /**
     * Exchanges the old vehicle for the new one, creating a new problem.
     *
     * @param oldVehicle the old vehicle
     * @param newVehicle the new vehicle
     * @return the updated problem
     */
    default Problem changeVehicle(Vehicle oldVehicle, Vehicle newVehicle) {
        String newName = newVehicle.getName();
        return changeActionObjectName(oldVehicle, newName).putVehicle(newName, newVehicle);
    }

    /**
     * Exchanges the old package for the new one, creating a new problem.
     *
     * @param oldPackage the old package
     * @param newPackage the new package
     * @return the updated problem
     */
    default Problem changePackage(Package oldPackage, Package newPackage) {
        String newName = newPackage.getName();
        return changeActionObjectName(oldPackage, newName).putPackage(newName, newPackage);
    }

    /**
     * Exchanges the old location for the new one, creating a new problem.
     *
     * @param oldLocation the old location
     * @param newLocation the new location
     * @return the updated problem
     */
    default Problem changeLocation(Location oldLocation, Location newLocation) {
        String newName = newLocation.getName();
        return changeActionObjectName(oldLocation, newName).putLocation(newName, newLocation);
    }

}
