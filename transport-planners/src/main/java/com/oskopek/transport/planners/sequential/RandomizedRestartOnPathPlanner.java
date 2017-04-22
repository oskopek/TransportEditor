package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.collection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

/**
 * Chooses a vehicle and package randomly,
 * find the path to deliver the package by the vehicle,
 * find all packages on it and around it
 * choose the ones whose target is on the path and deliver them.
 */
public class RandomizedRestartOnPathPlanner extends SequentialRandomizedPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor.
     */
    public RandomizedRestartOnPathPlanner() {
        setName(RandomizedRestartOnPathPlanner.class.getSimpleName());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(problem);
        logger.debug("Starting planning...");

        List<Vehicle> vehicles = new ArrayList<>(problem.getAllVehicles());
        while (true) {
            ImmutablePlanState current = new ImmutablePlanState(problem);
            while (!current.isGoalState() && current.getTotalTime() < getBestPlanScore()) {
                Problem curProblem = current.getProblem();
                List<Package> unfinished = new ArrayList<>(
                        PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                if (unfinished.isEmpty()) {
                    throw new IllegalStateException("Zero packages left but not in goal state.");
                }

                Vehicle chosenVehicle = vehicles.get(getRandom().nextInt(vehicles.size()));
                Package chosenPackage;
                while (true) {
                    chosenPackage = unfinished.get(getRandom().nextInt(unfinished.size()));
                    int curCapacity = chosenVehicle.getCurCapacity().getCost();
                    curCapacity -= chosenPackage.getSize().getCost();
                    if (curCapacity >= 0) {
                        break;
                    }
                }

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                        unfinished, false);
                current = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to state."));

                if (shouldCancel()) {
                    logger.debug("Cancelling, returning best found plan so far with score: {}.", getBestPlanScore());
                    return Optional.ofNullable(getBestPlan());
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Finished one iteration. Length: {}", current.getTotalTime());
            }

            int totalTime = current.getTotalTime();
            if (getBestPlanScore() > totalTime) {
                logger.debug("Found new best plan {} -> {}", getBestPlanScore(), totalTime);
                savePlanIfBetter(totalTime, new SequentialPlan(current.getAllActionsInList()));
            }
        }
    }

    @Override
    public RandomizedRestartOnPathPlanner copy() {
        return new RandomizedRestartOnPathPlanner();
    }
}
