package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An immutable {@link PlanState} implementation used for planning.
 */
public class ImmutablePlanState extends ProblemPlanningWrapper implements Problem {
    // TODO: consolidate with the plan state interface

    private final Domain domain;
    private final transient Logger logger = LoggerFactory.getLogger(ImmutablePlanState.class);
    private final List<Action> actions;
    private final int totalTime;

    /**
     * Default constructor.
     *
     * @param domain the domain
     * @param problem the problem
     * @param actions the plan actions
     */
    public ImmutablePlanState(Domain domain, Problem problem, List<Action> actions) {
        super(problem);
        this.domain = domain;
        this.actions = actions;
        totalTime = actions.stream().mapToInt(a -> a.getDuration().getCost()).sum(); // TODO: non sequential domains?
    }

    public ImmutablePlanState(ImmutablePlanState oldState, Problem newProblem, Action addedAction) {
        super(newProblem);
        this.domain = oldState.getDomain();
        this.actions = new ArrayList<>(oldState.getActions());
        this.actions.add(addedAction);
        totalTime = oldState.getTotalTime() + addedAction.getDuration().getCost(); // TODO: non sequential domains?
    }

    public ImmutablePlanState(ImmutablePlanState oldState, Problem newProblem) {
        super(newProblem);
        this.domain = oldState.getDomain();
        this.actions = oldState.getActions();
        totalTime = oldState.getTotalTime();
    }

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
     * Get the domain.
     *
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }

    public Optional<ImmutablePlanState> apply(Action action) {
        return applyPreconditions(action).flatMap(t -> t.applyEffects(action));
    }

    private Optional<ImmutablePlanState> applyPreconditions(Action action) {
        logger.trace("Checking preconditions of action {}.", action.getName());
        if (!action.arePreconditionsValid(getProblem())) {
            logger.trace("Preconditions of action " + action + " are invalid in problem " + getProblem());
            return Optional.empty();
        }
        logger.trace("Applying preconditions of action {}.", action.getName());
        return Optional.of(new ImmutablePlanState(this, action.applyPreconditions(domain, getProblem())));
    }

    private Optional<ImmutablePlanState> applyEffects(Action action) {
        logger.trace("Applying effects of action {}.", action.getName());
        Problem newProblem = action.applyEffects(domain, getProblem());
        logger.trace("Checking effects of action {}.", action.getName());
        if (!action.areEffectsValid(newProblem)) {
            logger.trace(
                    "Effects of action " + action + " are invalid after applying to problem " + getProblem()
                            + "(result: " + newProblem + ").");
            return Optional.empty();
        }
        return Optional.of(new ImmutablePlanState(this, newProblem, action));
    }


    @Override
    public ImmutablePlanState putVehicle(String name, Vehicle vehicle) {
        return new ImmutablePlanState(getDomain(), getProblem().putVehicle(name, vehicle), getActions());
    }

    @Override
    public ImmutablePlanState putPackage(String name, com.oskopek.transporteditor.model.problem.Package pkg) {
        return new ImmutablePlanState(getDomain(), getProblem().putPackage(name, pkg), getActions());
    }

    @Override
    public ImmutablePlanState changeActionObjectName(ActionObject actionObject, String newName) {
        return new ImmutablePlanState(getDomain(), getProblem().changeActionObjectName(actionObject, newName),
                getActions());
    }

    @Override
    public ImmutablePlanState putLocation(String name, Location location) {
        return new ImmutablePlanState(getDomain(), getProblem().putLocation(name, location), getActions());
    }

    @Override
    public ImmutablePlanState removeVehicle(String name) {
        return new ImmutablePlanState(getDomain(), getProblem().removeVehicle(name), getActions());
    }

    @Override
    public ImmutablePlanState removePackage(String name) {
        return new ImmutablePlanState(getDomain(), getProblem().removePackage(name), getActions());
    }

    @Override
    public ImmutablePlanState removeLocation(String name) {
        return new ImmutablePlanState(getDomain(), getProblem().removeLocation(name), getActions());
    }

    @Override
    public ImmutablePlanState putName(String newName) {
        return new ImmutablePlanState(getDomain(), getProblem().putName(newName), getActions());
    }

}
