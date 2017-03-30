package com.oskopek.transport.model.state;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * A default {@link PlanState} implementation for sequential and temporal domains.
 */
public class DefaultPlanState implements PlanState {

    private final Domain domain;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Problem problem;

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem
     */
    public DefaultPlanState(Domain domain, Problem problem) {
        this.domain = domain;
        this.problem = problem;
    }

    @Override
    public void applyPreconditions(Action action) {
        logger.trace("Checking preconditions of action {}.", action.getName());
        if (!action.arePreconditionsValid(problem)) {
            throw new IllegalStateException("Preconditions of action " + action + " are invalid in problem " + problem);
        }
        logger.trace("Applying preconditions of action {}.", action.getName());
        problem = action.applyPreconditions(domain, problem);
        logger.trace("Applied preconditions of action {}.", action.getName());
    }

    @Override
    public void applyEffects(Action action) {
        logger.trace("Applying effects of action {}.", action.getName());
        Problem newProblem = action.applyEffects(domain, problem);
        logger.trace("Checking effects of action {}.", action.getName());
        if (!action.areEffectsValid(newProblem)) {
            throw new IllegalStateException(
                    "Effects of action " + action + " are invalid after applying to problem " + problem + "(result: "
                            + newProblem + ").");
        }
        problem = newProblem;
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
    public Problem putName(String newName) {
        return problem.putName(newName);
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
        if (!(o instanceof DefaultPlanState)) {
            return false;
        }
        DefaultPlanState that = (DefaultPlanState) o;
        return new EqualsBuilder().append(domain, that.domain).append(problem, that.problem).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(domain).append(problem)
                .toHashCode();
    }
}
