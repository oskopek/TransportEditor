package com.oskopek.transport.planners.sequential.state;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.state.PlanState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An immutable {@link PlanState} implementation used for planning.
 */
public class ImmutablePlanState extends ProblemPlanningWrapper implements Problem {

    private final Domain domain;
    private final transient Logger logger = LoggerFactory.getLogger(ImmutablePlanState.class);
    private final List<Action> actions; // TODO: non sequential domains?
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
        super(newProblem);
        this.domain = oldState.getDomain();
        this.actions = new ArrayList<>(oldState.getActions());
        this.actions.add(addedAction);
        totalTime = oldState.getTotalTime() + addedAction.getDuration().getCost();
    }

    /**
     * Constructor for updating the problem.
     * @param oldState the old state to copy from
     * @param newProblem the new problem
     */
    public ImmutablePlanState(ImmutablePlanState oldState, Problem newProblem) {
        super(newProblem);
        this.domain = oldState.getDomain();
        this.actions = oldState.getActions();
        totalTime = oldState.getTotalTime();
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
     * Get the domain.
     *
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
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
        if (logger.isTraceEnabled()) {
            logger.trace("Checking preconditions of action {}.", action.getName());
        }
        if (!action.arePreconditionsValid(getProblem())) {
            if (logger.isTraceEnabled()) {
                logger.trace("Preconditions of action " + action + " are invalid in problem " + getProblem());
            }
            return Optional.empty();
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Applying preconditions of action {}.", action.getName());
        }
        return Optional.of(new ImmutablePlanState(this, action.applyPreconditions(domain, getProblem())));
    }

    /**
     * Applies the specified action's effects and returns the new state.
     *
     * @param action the action's effects to apply
     * @return the updated state or empty if the effects were not valid after application
     */
    private Optional<ImmutablePlanState> applyEffects(Action action) {
        if (logger.isTraceEnabled()) {
            logger.trace("Applying effects of action {}.", action.getName());
        }
        Problem newProblem = action.applyEffects(domain, getProblem());
        if (logger.isTraceEnabled()) {
            logger.trace("Checking effects of action {}.", action.getName());
        }
        if (!action.areEffectsValid(newProblem)) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Effects of action " + action + " are invalid after applying to problem " + getProblem()
                                + "(result: " + newProblem + ").");
            }
            return Optional.empty();
        }
        return Optional.of(new ImmutablePlanState(this, newProblem, action));
    }

    @Override
    public ImmutablePlanState putVehicle(String name, Vehicle vehicle) {
        return new ImmutablePlanState(getDomain(), getProblem().putVehicle(name, vehicle), getActions());
    }

    @Override
    public ImmutablePlanState putPackage(String name, com.oskopek.transport.model.problem.Package pkg) {
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
