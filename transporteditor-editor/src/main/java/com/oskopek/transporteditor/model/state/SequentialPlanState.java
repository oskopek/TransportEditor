package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class SequentialPlanState implements PlanState {

    private final Domain origDomain;
    private final Problem origProblem;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Problem problem;

    public SequentialPlanState(Domain domain, Problem problem) {
        this.origDomain = domain;
        this.origProblem = problem;
        this.problem = origProblem;
    }

    @Override
    public void apply(Action action) {
        logger.debug("Checking preconditions of action {}.", action.getName());
        if (!action.arePreconditionsValid(problem)) {
            throw new IllegalStateException("Preconditions of action " + action + " are invalid in problem " + problem);
        }
        logger.debug("Applying action {}.", action.getName());
        Problem newProblem = action.apply(origDomain, problem);
        logger.debug("Checking effects of action {}.", action.getName());
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
}
