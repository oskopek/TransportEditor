package com.oskopek.transport.planners.sequential.state;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.graph.VisualRoadGraph;
import com.oskopek.transport.model.state.PlanState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * An immutable {@link PlanState} implementation used for planning.
 * <p>
 * Wrapper of a problem used in planning: does not use standard hash code and equals,
 * but assumes the graph (and other things, like the action object names) do not change and does not compare them.
 */
public class ImmutablePlanState implements Problem {

    private final List<Action> actions; // TODO: non sequential domains?
    private final int totalTime;

    private final Problem problem; // TODO: do not store the entire problem?
    private int planningHashCode;

    /**
     * Default constructor.
     *
     * @param problem the problem
     * @param actions the plan actions
     */
    public ImmutablePlanState(Problem problem, List<Action> actions) {
        this.problem = problem;
        this.actions = actions;
        totalTime = actions.stream().mapToInt(a -> a.getDuration().getCost()).sum();
    }

    /**
     * Constructor for updating the problem and appending an action.
     *
     * @param oldState the old state to copy all properties from
     * @param newProblem the new problem
     * @param addedAction the added action
     */
    public ImmutablePlanState(ImmutablePlanState oldState, Problem newProblem, Action addedAction) {
        this.problem = newProblem;
        this.actions = new ArrayList<>(oldState.actions);
        this.actions.add(addedAction);
        totalTime = oldState.totalTime + addedAction.getDuration().getCost();
    }

    /**
     * Constructor for updating the problem.
     *
     * @param oldState the old state to copy from
     * @param newProblem the new problem
     */
    public ImmutablePlanState(ImmutablePlanState oldState, Problem newProblem) {
        this.problem = newProblem;
        this.actions = oldState.actions;
        totalTime = oldState.totalTime;
    }

    /**
     * Get the pre-calculated total time it takes this plan to reach this state.
     *
     * @return the total time
     */
    public int getTotalTime() {
        return totalTime;
    }

    /**
     * Get the actions.
     *
     * @return the actions
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Applies the specified action and returns the new state.
     *
     * @param action the action to apply
     * @return the updated state or empty if the preconditions or the effects were not valid in the resulting state
     */
    public Optional<ImmutablePlanState> apply(Action action) {
        return applyPreconditions(action).flatMap(t -> t.applyEffects(action));
    }

    /**
     * Applies the specified action's preconditions and returns the new state.
     *
     * @param action the action's preconditions to apply
     * @return the updated state or empty if the preconditions were not valid before application
     */
    private Optional<ImmutablePlanState> applyPreconditions(Action action) {
        if (!action.arePreconditionsValid(getProblem())) {
            return Optional.empty();
        }
        return Optional.of(new ImmutablePlanState(this, action.applyPreconditions(getProblem())));
    }

    /**
     * Applies the specified action's effects and returns the new state.
     *
     * @param action the action's effects to apply
     * @return the updated state or empty if the effects were not valid after application
     */
    private Optional<ImmutablePlanState> applyEffects(Action action) {
        Problem newProblem = action.applyEffects(getProblem());
        if (!action.areEffectsValid(newProblem)) {
            return Optional.empty();
        }
        return Optional.of(new ImmutablePlanState(this, newProblem, action));
    }

    @Override
    public ImmutablePlanState putVehicle(String name, Vehicle vehicle) {
        return new ImmutablePlanState(getProblem().putVehicle(name, vehicle), actions);
    }

    @Override
    public ImmutablePlanState putPackage(String name, com.oskopek.transport.model.problem.Package pkg) {
        return new ImmutablePlanState(getProblem().putPackage(name, pkg), actions);
    }

    @Override
    public ImmutablePlanState changeActionObjectName(ActionObject actionObject, String newName) {
        return new ImmutablePlanState(getProblem().changeActionObjectName(actionObject, newName), actions);
    }

    @Override
    public ImmutablePlanState putLocation(String name, Location location) {
        return new ImmutablePlanState(getProblem().putLocation(name, location), actions);
    }

    @Override
    public ImmutablePlanState removeVehicle(String name) {
        return new ImmutablePlanState(getProblem().removeVehicle(name), actions);
    }

    @Override
    public ImmutablePlanState removePackage(String name) {
        return new ImmutablePlanState(getProblem().removePackage(name), actions);
    }

    @Override
    public ImmutablePlanState removeLocation(String name) {
        return new ImmutablePlanState(getProblem().removeLocation(name), actions);
    }

    @Override
    public ImmutablePlanState putName(String newName) {
        return new ImmutablePlanState(getProblem().putName(newName), actions);
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
        for (com.oskopek.transport.model.problem.Package p : getAllPackages()) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutablePlanState)) {
            return false;
        }
        ImmutablePlanState that = (ImmutablePlanState) o;
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

        for (Vehicle vehicle : problem.getVehicleMap().values()) {
            String vehicleName = vehicle.getName();
            Vehicle otherVehicle = otherVehicleMap.get(vehicleName);
            if (otherVehicle == null || !otherVehicle.getLocation().getName().equals(vehicle.getLocation().getName())) {
                return false;
            }

            Collection<Package> packages = vehicle.getPackageList();
            Set<String> pkgs = new HashSet<>(packages.size());
            for (Package pkg : packages) {
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
