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
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

/**
 * Chooses a package randomly,
 * choose a nearby or random vehicle (based on a biased coin flip),
 * find the path to deliver the package by the vehicle,
 * find all packages on it and around it
 * choose the ones whose target is on the path and deliver them.
 */
public class RandomizedRestartOnPathNearbyPlanner extends SequentialRandomizedPlanner {

    /**
     * Default constructor.
     */
    public RandomizedRestartOnPathNearbyPlanner() {
        setName(RandomizedRestartOnPathNearbyPlanner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        formatLog("Initializing planning...");
        resetState();
        initialize(problem);
        formatLog("Starting planning...");

        List<Vehicle> vehicles = new ArrayList<>(problem.getAllVehicles());
        float exploration = 0.2f; // best so far: 0.2 or 0.1
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
                Vehicle chosenVehicle = chooseVehicle(curProblem, vehicles, chosenPackage, exploration);
                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                        unfinished, false);
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

    /**
     * Choose a vehicle using a biased coin toss.
     *
     * @param curProblem the current problem state
     * @param vehicles the vehicles (expects stable order between calls)
     * @param chosenPackage the chosen package
     * @param exploration the exploration hyperparameter [0,1] for random vehicle choice (lower = less chance)
     * @return the chosen vehicle with enough capacity, or null
     */
    protected Vehicle chooseVehicle(Problem curProblem, List<Vehicle> vehicles, Package chosenPackage,
            float exploration) {
        Vehicle chosenVehicle;
        while (true) {
            if (getRandom().nextFloat() < exploration) {
                chosenVehicle = vehicles.get(getRandom().nextInt(vehicles.size()));
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
        return chosenVehicle;
    }

    @Override
    public RandomizedRestartOnPathNearbyPlanner copy() {
        return new RandomizedRestartOnPathNearbyPlanner();
    }
}
