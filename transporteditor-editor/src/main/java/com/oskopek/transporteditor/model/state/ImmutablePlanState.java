package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import javaslang.collection.List;
import javaslang.control.Either;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * An immutable {@link PlanState} implementation used for planning.
 */
public class ImmutablePlanState implements Problem { // TODO: consolidate with the plan state interface

    private final Domain domain;
    private final transient Logger logger = LoggerFactory.getLogger(ImmutablePlanState.class);
    private final Problem problem;
    private final List<Action> actions;

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem
     */
    public ImmutablePlanState(Domain domain, Problem problem, List<Action> actions) {
        this.domain = domain;
        this.problem = problem;
        this.actions = actions;
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
     * Get the domain.
     *
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }

    public boolean isGoalState() {
        return getAllPackages().stream().allMatch(p -> p.getTarget().equals(p.getLocation()));
    }

    public Optional<ImmutablePlanState> apply(Action action) {
        return applyPreconditions(action).flatMap(t -> t.applyEffects(action));
    }

    private Optional<ImmutablePlanState> applyPreconditions(Action action) {
        logger.trace("Checking preconditions of action {}.", action.getName());
        if (!action.arePreconditionsValid(problem)) {
            logger.trace("Preconditions of action " + action + " are invalid in problem " + problem);
            return Optional.empty();
        }
        logger.trace("Applying preconditions of action {}.", action.getName());
        return Optional.of(new ImmutablePlanState(domain, action.applyPreconditions(domain, problem), actions));
    }

    private Optional<ImmutablePlanState> applyEffects(Action action) {
        logger.trace("Applying effects of action {}.", action.getName());
        Problem newProblem = action.applyEffects(domain, problem);
        logger.trace("Checking effects of action {}.", action.getName());
        if (!action.areEffectsValid(newProblem)) {
            logger.trace(
                    "Effects of action " + action + " are invalid after applying to problem " + problem + "(result: "
                            + newProblem + ").");
            return Optional.empty();
        }
        return Optional.of(new ImmutablePlanState(domain, newProblem, actions.append(action)));
    }

    @Override
    public String getName() {
        return problem.getName();
    }

    @Override
    public RoadGraph getRoadGraph() {
        return problem.getRoadGraph();
    }

    @Override
    public Vehicle getVehicle(String name) {
        return problem.getVehicle(name);
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
    public Problem putVehicle(String name, Vehicle vehicle) {
        return problem.putVehicle(name, vehicle);
    }

    @Override
    public Problem putPackage(String name, Package pkg) {
        return problem.putPackage(name, pkg);
    }

    @Override
    public Problem changeActionObjectName(ActionObject actionObject, String newName) {
        return problem.changeActionObjectName(actionObject, newName);
    }

    @Override
    public Problem putLocation(String name, Location location) {
        return problem.putLocation(name, location);
    }

    @Override
    public Problem removeVehicle(String name) {
        return problem.removeVehicle(name);
    }

    @Override
    public Problem removePackage(String name) {
        return problem.removePackage(name);
    }

    @Override
    public Problem removeLocation(String name) {
        return problem.removeLocation(name);
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
        return new EqualsBuilder().append(domain, that.domain).append(problem, that.problem).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(domain).append(problem)
                .toHashCode();
    }
}
