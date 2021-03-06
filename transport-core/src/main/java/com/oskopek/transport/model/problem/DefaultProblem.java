package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.graph.VisualRoadGraph;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Default problem implementation. Provides basic data properties and immutable update methods.
 */
public class DefaultProblem implements Problem {

    private final String name;
    private final RoadGraph roadGraph;
    private final Map<String, Vehicle> vehicleMap;
    private final Map<String, Package> packageMap;

    /**
     * Copy constructor.
     *
     * @param problem the problem to copy
     */
    public DefaultProblem(Problem problem) {
        // vehicles are immutable
        // packages are immutable
        this(problem.getName(), problem.getRoadGraph().copy(),
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
    public DefaultProblem(String name, RoadGraph roadGraph, Map<String, Vehicle> vehicleMap,
            Map<String, Package> packageMap) {
        this.name = name;
        this.roadGraph = roadGraph;
        this.vehicleMap = vehicleMap;
        this.packageMap = packageMap;
    }

    @Override
    public VisualRoadGraph getVisualRoadGraph() {
        return null;
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
    public Problem putVehicle(String name, Vehicle vehicle) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(vehicleMap);
        newVehicleMap.put(name, vehicle);
        return new DefaultProblem(this.name, roadGraph, newVehicleMap, packageMap);
    }

    /**
     * Put all vehicles in the stream.
     *
     * @param vehicles the vehicles to put, keys are their names
     * @return the updated problem
     * @see #putVehicle(String, Vehicle)
     */
    public Problem putAllVehicles(Stream<Vehicle> vehicles) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(vehicleMap);
        vehicles.forEach(v -> newVehicleMap.put(v.getName(), v));
        return new DefaultProblem(name, roadGraph, newVehicleMap, packageMap);
    }

    @Override
    public Problem putPackage(String name, Package pkg) {
        Map<String, Package> newPackageMap = new HashMap<>(packageMap);
        newPackageMap.put(name, pkg);
        return new DefaultProblem(this.name, roadGraph, vehicleMap, newPackageMap);
    }

    @Override
    public Problem changeActionObjectName(ActionObject actionObject, String newName) {
        if (actionObject.getName().equals(newName)) {
            return this;
        } else {
            throw new UnsupportedOperationException("Cannot change action object name.");
        }

//        if (actionObject instanceof Package) {
//            Package oldPackage = (Package) actionObject;
//            Package newPackage = oldPackage.updateName(newName);
//
//            Map<String, Package> newPackageMap = new HashMap<>(getPackageMap());
//            newPackageMap.remove(oldPackage.getName());
//            newPackageMap.put(newPackage.getName(), newPackage);
//            DefaultProblem next = new DefaultProblem(getName(), getRoadGraph(), getVehicleMap(), newPackageMap);
//
//            return next.putAllVehicles(getAllVehicles().stream().filter(v -> v.getPackageList().contains(oldPackage))
//                    .map(v -> v.changePackage(oldPackage, newPackage)));
//        } else if (actionObject instanceof Vehicle) {
//            Vehicle oldVehicle = (Vehicle) actionObject;
//            Vehicle newVehicle = oldVehicle.updateName(newName);
//
//            Map<String, Vehicle> newVehicleMap = new HashMap<>(getVehicleMap());
//            newVehicleMap.remove(oldVehicle.getName());
//            newVehicleMap.put(newVehicle.getName(), newVehicle);
//            return new DefaultProblem(getName(), getRoadGraph(), newVehicleMap, getPackageMap());
//        } else if (actionObject instanceof Location) {
//            throw new UnsupportedOperationException("Changing location names is not supported.");
//        } else if (actionObject instanceof Road) { // TODO: Should this be immutable too? Yes!
//            RoadEdge edge = getRoadGraph().getRoadEdge(actionObject.getName());
//            getRoadGraph().removeRoad(actionObject.getName());
//            if (actionObject instanceof FuelRoad) {
//                FuelRoad fuelRoad = (FuelRoad) actionObject;
//                getRoadGraph().addRoad(fuelRoad.updateName(newName), edge.getFrom(), edge.getTo());
//            } else if (actionObject instanceof DefaultRoad) {
//                DefaultRoad defaultRoad = (DefaultRoad) actionObject;
//                getRoadGraph().addRoad(defaultRoad.updateName(newName), edge.getFrom(), edge.getTo());
//            }
//            return this;
//        } else {
//            throw new IllegalStateException("Could not determine action object type.");
//        }
    }

    @Override
    public Problem removeVehicle(String name) {
        Map<String, Vehicle> newVehicleMap = new HashMap<>(vehicleMap);
        newVehicleMap.remove(name);
        return new DefaultProblem(this.name, roadGraph, newVehicleMap, packageMap);
    }

    @Override
    public Problem removePackage(String name) {
        Map<String, Package> newPackageMap = new HashMap<>(packageMap);
        newPackageMap.remove(name);
        return new DefaultProblem(this.name, roadGraph, vehicleMap, newPackageMap);
    }

    @Override
    public Problem removeLocation(String name) { // TODO: Should this be immutable too? Yes!
        roadGraph.removeLocation(roadGraph.getLocation(name));
        return this;
    }

    @Override
    public Problem putLocation(String name, Location location) { // TODO: Should this be immutable too? Yes!
        roadGraph.moveLocation(name, location.getxCoordinate(), location.getyCoordinate());
        roadGraph.setPetrolStation(name, location.getPetrolStation());
        return this;
    }

    @Override
    public Problem putName(String newName) {
        return new DefaultProblem(newName, roadGraph, vehicleMap, packageMap);
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
        if (!(o instanceof DefaultProblem)) {
            return false;
        }
        DefaultProblem that = (DefaultProblem) o;
        return new EqualsBuilder().append(name, that.name).append(roadGraph, that.roadGraph).append(
                vehicleMap, that.vehicleMap).append(packageMap, that.packageMap).isEquals();
    }
}
