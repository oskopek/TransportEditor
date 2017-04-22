package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.collection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * Chooses a package randomly and choose a nearby vehicle or a random one,
 * based on a biased coin flip.
 * Find the path to deliver the package by the vehicle,
 * find all packages on it and around it
 * choose the ones whose target is on the path
 * if still not fully capacitated, choose the others in the
 * order of distance from their destinations at any possible drop of point on the path.
 */
public class RandomizedRestartWithAroundPathPickupPlanner extends SequentialRandomizedPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor.
     */
    public RandomizedRestartWithAroundPathPickupPlanner() {
        setName(RandomizedRestartWithAroundPathPickupPlanner.class.getSimpleName());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        return plan(domain, problem, Function.identity());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem, Function<Plan, Plan> planTransformation) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(problem);
        logger.debug("Starting planning...");

        List<Vehicle> vehicles = new ArrayList<>(problem.getAllVehicles());
        int i = 1;
//        float exploration = 0.2f; // best so far: 0.2 or 0.1
//        float multiplier = 0.00f; // best so far: 0
//        int everySteps = 50_000;
        float exploration = 0.8f; // best so far: 0.2 or 0.1
        float multiplier = 0.05f; // best so far: 0
        int everySteps = 10_000;
        while (true) {
            if (i % everySteps == 0) {
                float delta = exploration;
                exploration -= delta * multiplier;
                logger.debug("Exploration increased to: {}", exploration);
            }
            i++;

            ImmutablePlanState current = new ImmutablePlanState(problem);
            double makeSpan = planTransformation.apply(new SequentialPlan(current.getAllActionsInList()))
                    .calculateMakespan();
            while (!current.isGoalState() && makeSpan < getBestPlanScore()) { // TODO: too slow for seq
                Problem curProblem = current.getProblem();
                List<Package> unfinished = new ArrayList<>(
                        PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                if (unfinished.isEmpty()) {
                    List<Drive> driveToTarget = new ArrayList<>(); // TODO: irrelevant for seq
                    for (Vehicle vehicle : curProblem.getAllVehicles()) {
                        if (vehicle.getTarget() != null && !vehicle.getTarget().equals(vehicle.getLocation())) {
                            List<RoadEdge> edges = getShortestPathMatrix().get(vehicle.getLocation().getName(),
                                    vehicle.getTarget().getName()).getRoads();
                            for (RoadEdge edge : edges) {
                                driveToTarget.add(domain.buildDrive(vehicle, edge.getFrom(), edge.getTo(),
                                        edge.getRoad()));
                            }
                        }
                    }

                    if (driveToTarget.isEmpty()) {
                        throw new IllegalStateException("Zero packages left and no vehicles not at targets but"
                                + " not in goal state.");
                    } else {
                        current = Stream.ofAll(driveToTarget).foldLeft(Optional.of(current),
                                (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                                .orElseThrow(() -> new IllegalStateException("Could not apply all new drive actions"
                                        + " to current state."));
                        break;
                    }
                }

                Package chosenPackage = unfinished.get(getRandom().nextInt(unfinished.size()));
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

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                        unfinished, true);
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

            if (current == null) { // invalid state, break
                continue;
            }

            // TODO: too slow for seq?
            Plan curPlan = planTransformation.apply(new SequentialPlan(current.getAllActionsInList()));
            if (curPlan == null) {
                continue;
            }
            double curScore = curPlan.calculateMakespan();
            if (curScore < getBestPlanScore()) {
                savePlanIfBetter(Math.round((float) curScore), curPlan);
            }
        }
    }

    @Override
    public RandomizedRestartWithAroundPathPickupPlanner copy() {
        return new RandomizedRestartWithAroundPathPickupPlanner();
    }
}
