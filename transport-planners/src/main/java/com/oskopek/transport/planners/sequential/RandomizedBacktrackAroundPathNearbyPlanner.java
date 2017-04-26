package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.collection.Stream;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Chooses a vehicle and package,
 * find the path to deliver the package by the vehicle,
 * find all packages on it and around it
 * choose the ones whose target is on the path
 * if still not fully capacitated, choose the others in the
 * order of distance from their destinations at any possible drop of point on the path.
 */
public class RandomizedBacktrackAroundPathNearbyPlanner extends SequentialRandomizedPlanner {

    /**
     * Default constructor.
     */
    public RandomizedBacktrackAroundPathNearbyPlanner() {
        setName(RandomizedBacktrackAroundPathNearbyPlanner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        formatLog("Initializing planning...");
        resetState();
        initialize(problem);
        formatLog("Starting planning...");
        return planRecursively(domain, problem);
    }

    /**
     * Wrapper around {@link #newStateRecursively(Domain, ImmutablePlanState, float)}.
     *
     * @param domain the domain
     * @param problem the problem
     * @return the plan, or an empty optional
     */
    private Optional<Plan> planRecursively(Domain domain, Problem problem) {
        float exploration = 0.8f;
        newStateRecursively(domain, new ImmutablePlanState(problem), exploration);
        return Optional.ofNullable(getBestPlan());
    }

    /**
     * Backtrack through generated actions to find a new state.
     *
     * @param domain the domain
     * @param current the current state
     * @param exploration the exploration factor
     * @return a new state, or null if we want to break the backtracking
     */
    private ImmutablePlanState newStateRecursively(Domain domain, final ImmutablePlanState current,
            final float exploration) {
        if (current.isGoalState()) {
            int totalTime = current.getTotalTime();
            if (getBestPlanScore() > totalTime) {
                formatLog("Found new best plan {} -> {}", getBestPlanScore(), totalTime);
                savePlanIfBetter(totalTime, new SequentialPlan(current.getAllActionsInList()));
            }
            return current;
        }
        if (shouldCancel()) {
            formatLog("Cancelling, returning best found plan so far with score: {}.", getBestPlanScore());
            return null;
        }
        if (current.getTotalTime() >= getBestPlanScore()) {
            return current;
        }


        Problem curProblem = current.getProblem();
        final List<Package> unfinished = new ArrayList<>(
                PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
        List<Package> toTry = new ArrayList<>(unfinished);
        if (unfinished.isEmpty()) {
            throw new IllegalStateException("Zero packages left but not in goal state.");
        }

        while (!toTry.isEmpty()) {
            Package chosenPackage = toTry.remove(getRandom().nextInt(toTry.size()));

            List<Vehicle> vehicles = new ArrayList<>(curProblem.getAllVehicles());
            Vehicle chosenVehicle;
            while (!vehicles.isEmpty()) {
                while (true) {
                    if (getRandom().nextFloat() < exploration) {
                        chosenVehicle = vehicles.remove(getRandom().nextInt(vehicles.size()));
                    } else {
                        Optional<Vehicle> maybeVehicle = nearestVehicle(curProblem.getAllVehicles(),
                                chosenPackage.getLocation(), chosenPackage.getSize().getCost());
                        if (maybeVehicle.isPresent()) {
                            chosenVehicle = maybeVehicle.get();
                        } else {
                            continue;
                        }
                    }
                    if (chosenVehicle.getCurCapacity().getCost() >= chosenPackage.getSize().getCost()) {
                        break;
                    }
                }

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                        unfinished, true);
                ImmutablePlanState newState = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to state."));
                ImmutablePlanState returned = newStateRecursively(domain, newState, exploration);
                if (returned == null) { // cancelling
                    return null;
                }
            }
        }
        return current;
    }

    @Override
    public RandomizedBacktrackAroundPathNearbyPlanner copy() {
        return new RandomizedBacktrackAroundPathNearbyPlanner();
    }

}
