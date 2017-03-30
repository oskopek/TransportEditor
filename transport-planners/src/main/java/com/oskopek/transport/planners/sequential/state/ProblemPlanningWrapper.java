package com.oskopek.transport.planners.sequential.state;

import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.graph.VisualRoadGraph;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * Wrapper of a problem used in planning: does not use standard hash code and equals,
 * but assumes the graph (and other things, like the action object names) do not change and does not compare them.
 */
public class ProblemPlanningWrapper implements Problem { // TODO: consolidate with the plan state interface

    private final Problem problem; // TODO: do not store the entire problem?
    private int planningHashCode;

    /**
     * Default constructor.
     *
     * @param problem the problem to wrap
     */
    public ProblemPlanningWrapper(Problem problem) {
        this.problem = problem;
    }

    @Override
    public VisualRoadGraph getVisualRoadGraph() {
        return problem.getVisualRoadGraph();
    }

    @Override
    public String getName() {
        return problem.getName();
    }

    @Override
    public RoadGraph getRoadGraph() {
        return problem.getRoadGraph();
    }

    /**
     * Check if this state is a goal state.
     *
     * @return true iff this state is a goal state, i.e. if all packages and vehicles are at their targets, if specified
     */
    public boolean isGoalState() { // TODO: merge this into the PlanState interface
        for (Package p : getAllPackages()) {
            if (!p.getTarget().equals(p.getLocation())) {
                return false;
            }
        }
        for (Vehicle v : getAllVehicles()) {
            Location target = v.getTarget();
            if (target != null && !target.equals(v.getLocation())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Utility method for getting a vehicle by its name.
     *
     * @param name the name of the vehicle to get
     * @return the vehicle
     * @throws IllegalArgumentException if a vehicle with that name wasn't found
     */
    public Vehicle getVehicleSafe(String name) {
        Vehicle vehicle = getVehicle(name);
        if (vehicle == null) {
            throw new IllegalArgumentException("Could not find vehicle with name \"" + name + "\".");
        }
        return vehicle;
    }

    /**
     * Get the internal problem that is wrapped.
     *
     * @return the internal problem
     */
    protected Problem getProblem() {
        return problem;
    }

    @Override
    public Vehicle getVehicle(String name) {
        return problem.getVehicle(name);
    }


    @Override
    public Package getPackage(String name) {
        return problem.getPackage(name);
    }

    @Override
    public Locatable getLocatable(String name) {
        return problem.getLocatable(name);
    }

    @Override
    public ActionObject getActionObject(String name) {
        return problem.getActionObject(name);
    }

    @Override
    public Collection<Vehicle> getAllVehicles() {
        return problem.getAllVehicles();
    }

    @Override
    public Map<String, Vehicle> getVehicleMap() {
        return problem.getVehicleMap();
    }

    @Override
    public Collection<Package> getAllPackages() {
        return problem.getAllPackages();
    }

    @Override
    public Map<String, Package> getPackageMap() {
        return problem.getPackageMap();
    }

    @Override
    public ProblemPlanningWrapper putVehicle(String name, Vehicle vehicle) {
        return new ProblemPlanningWrapper(problem.putVehicle(name, vehicle));
    }

    @Override
    public ProblemPlanningWrapper putPackage(String name, Package pkg) {
        return new ProblemPlanningWrapper(problem.putPackage(name, pkg));
    }

    @Override
    public ProblemPlanningWrapper changeActionObjectName(ActionObject actionObject, String newName) {
        return new ProblemPlanningWrapper(problem.changeActionObjectName(actionObject, newName));
    }

    @Override
    public ProblemPlanningWrapper putLocation(String name, Location location) {
        return new ProblemPlanningWrapper(problem.putLocation(name, location));
    }

    @Override
    public ProblemPlanningWrapper removeVehicle(String name) {
        return new ProblemPlanningWrapper(problem.removeVehicle(name));
    }

    @Override
    public ProblemPlanningWrapper removePackage(String name) {
        return new ProblemPlanningWrapper(problem.removePackage(name));
    }

    @Override
    public ProblemPlanningWrapper removeLocation(String name) {
        return new ProblemPlanningWrapper(problem.removeLocation(name));
    }

    @Override
    public ProblemPlanningWrapper putName(String newName) {
        return new ProblemPlanningWrapper(problem.putName(newName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProblemPlanningWrapper)) {
            return false;
        }
        ProblemPlanningWrapper that = (ProblemPlanningWrapper) o;
        return equalsDuringPlanning(problem, that.problem);
    }

    /**
     * Internal implementation of a fast assumption-dependent equals. For example, totally omits the graph
     * from comparison.
     *
     * @param problem the problem to compare
     * @param other the other problem to compare
     * @return true iff they are equal, assuming all assumptions hold
     */
    private static boolean equalsDuringPlanning(Problem problem, Problem other) {
        if (hashCodeDuringPlanning(problem) != hashCodeDuringPlanning(other)) {
            return false;
        }
        Map<String, Vehicle> otherVehicleMap = other.getVehicleMap();
        Set<String> pkgs = new HashSet<>();
        for (Vehicle vehicle : problem.getVehicleMap().values()) {
            String vehicleName = vehicle.getName();
            Vehicle otherVehicle = otherVehicleMap.get(vehicleName);
            if (otherVehicle == null || !otherVehicle.getLocation().getName().equals(vehicle.getLocation().getName())) {
                return false;
            }

            for (Package pkg : vehicle.getPackageList()) {
                pkgs.add(pkg.getName());
            }
            for (Package pkg : otherVehicle.getPackageList()) {
                if (!pkgs.remove(pkg.getName())) {
                    return false;
                }
            }
            if (!pkgs.isEmpty()) {
                return false;
            }
        }

        Map<String, Package> otherPackageMap = other.getPackageMap();
        for (Package pkg : problem.getPackageMap().values()) {
            Package otherPkg = otherPackageMap.get(pkg.getName());
            if (otherPkg == null || !Objects.equals(pkg.getLocation(), otherPkg.getLocation())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (planningHashCode == 0) {
            planningHashCode = hashCodeDuringPlanning(problem);
        }
        return planningHashCode;
    }

    /**
     * Internal implementation of a fast assumption-dependent hash code.
     *
     * @param problem the problem to calculate a hash code for
     * @return the hash code
     */
    private static int hashCodeDuringPlanning(Problem problem) {
        HashCodeBuilder builder = new HashCodeBuilder(13, 17);
        for (Vehicle vehicle : problem.getVehicleMap().values()) {
            builder.append(vehicle.getName());
            String location = vehicle.getLocation().getName();
            builder.append(location);
            vehicle.getPackageList().forEach(p -> packageHashCodeDuringPlanning(builder, p, location));
        }

        for (Package pkg : problem.getPackageMap().values()) {
            if (pkg.getLocation() != null) {
                packageHashCodeDuringPlanning(builder, pkg, pkg.getLocation().getName());
            }
        }
        return builder.toHashCode();
    }

    /**
     * Util method for calculating a packages hash code fast. Assumption-dependent.
     *
     * @param builder the builder to append to
     * @param pkg the package
     * @param location the location of the package
     */
    private static void packageHashCodeDuringPlanning(HashCodeBuilder builder, Package pkg, String location) {
        builder.append(pkg.getName());
        builder.append(location);
    }

}
