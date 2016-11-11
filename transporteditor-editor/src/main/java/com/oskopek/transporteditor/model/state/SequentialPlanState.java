package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;

import java.util.Collection;
import java.util.Map;

public class SequentialPlanState implements PlanState {

    private final Domain origDomain;
    private final Problem origProblem;
    private Problem problem;

    public SequentialPlanState(Domain domain, Problem problem) {
        this.origDomain = domain;
        this.origProblem = problem;
        this.problem = origProblem;
    }

    @Override
    public void apply(Action action) {
        if (!action.arePreconditionsValid(problem)) {
            throw new IllegalStateException("Preconditions of action " + action + " are invalid in problem " + problem);
        }
        Problem newProblem = action.apply(origDomain, problem);
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
    public Problem updateVehicle(String name, Vehicle vehicle) {
        return problem.updateVehicle(name, vehicle);
    }

    @Override
    public Problem updatePackage(String name, Package pkg) {
        return problem.updatePackage(name, pkg);
    }
}
