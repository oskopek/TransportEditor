package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.collection.Stream;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Chooses a package randomly and sample a nearby vehicle from a distribution based on inverse distance to the package,
 * find the path to deliver the package by the vehicle,
 * find all packages on it and around it
 * choose the ones whose target is on the path
 * if still not fully capacitated, choose the others in the
 * order of distance from their destinations at any possible drop of point on the path.
 */
public class RandomizedRestartAroundPathDistributionPlanner extends SequentialRandomizedPlanner {

    /**
     * Default constructor.
     */
    public RandomizedRestartAroundPathDistributionPlanner() {
        setName(RandomizedRestartAroundPathDistributionPlanner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        formatLog("Initializing planning...");
        resetState();
        initialize(problem);
        formatLog("Starting planning...");
        double temperature = 0.1d; // hyperparameter
        while (true) {
            ImmutablePlanState current = new ImmutablePlanState(problem);
            while (!current.isGoalState() && current.getTotalTime() < getBestPlanScore()) {
                Problem curProblem = current.getProblem();
                List<Package> unfinished = new ArrayList<>(
                        PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                if (unfinished.isEmpty()) {
                    throw new IllegalStateException("Zero packages left but not in goal state.");
                }

                Package chosenPackage = unfinished.get(getRandom().nextInt(unfinished.size()));
                Vehicle chosenVehicle;
                while (true) {
                    chosenVehicle = chooseFromDistanceDistribution(Stream.ofAll(curProblem.getAllVehicles())
                                    .filter(v -> v.getCurCapacity().getCost() >= chosenPackage.getSize().getCost()),
                            chosenPackage.getLocation(), temperature, false);
                    if (chosenVehicle.getCurCapacity().getCost() >= chosenPackage.getSize().getCost()) {
                        break;
                    }
                }

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                        unfinished, true);
                current = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to state."));

                if (shouldCancel()) {
                    formatLog("Cancelling, returning best found plan so far with score: {}.", getBestPlanScore());
                    return Optional.ofNullable(getBestPlan());
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Finished one iteration. Length: {}", current.getTotalTime());
            }

            if (!current.isGoalState()) {
                continue;
            }
            int totalTime = current.getTotalTime();
            if (getBestPlanScore() > totalTime) {
                savePlanIfBetter(totalTime, new SequentialPlan(current.getAllActionsInList()));
            }
        }
    }

    @Override
    public RandomizedRestartAroundPathDistributionPlanner copy() {
        return new RandomizedRestartAroundPathDistributionPlanner();
    }
}
