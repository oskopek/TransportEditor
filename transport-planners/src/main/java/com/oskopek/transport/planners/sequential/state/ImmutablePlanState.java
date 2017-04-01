package com.oskopek.transport.planners.sequential.state;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.state.PlanState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An immutable {@link PlanState} implementation used for planning.
 */
public class ImmutablePlanState extends ProblemPlanningWrapper implements Problem {

    private final List<Action> actions; // TODO: non sequential domains?
    private final int totalTime;

    /**
     * Default constructor.
     *
     * @param problem the problem
     * @param actions the plan actions
     */
    public ImmutablePlanState(Problem problem, List<Action> actions) {
        super(problem);
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
        this.actions = new ArrayList<>(oldState.actions);
        this.actions.add(addedAction);
        totalTime = oldState.totalTime + addedAction.getDuration().getCost();
    }

    /**
     * Constructor for updating the problem.
     * @param oldState the old state to copy from
     * @param newProblem the new problem
     */
    public ImmutablePlanState(ImmutablePlanState oldState, Problem newProblem) {
        super(newProblem);
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

}
