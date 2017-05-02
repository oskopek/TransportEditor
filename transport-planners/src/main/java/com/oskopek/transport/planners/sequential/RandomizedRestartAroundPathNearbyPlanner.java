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
public class RandomizedRestartAroundPathNearbyPlanner extends RandomizedRestartOnPathNearbyPlanner {

    /**
     * Default constructor.
     */
    public RandomizedRestartAroundPathNearbyPlanner() {
        setName(RandomizedRestartAroundPathNearbyPlanner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        return planWithOptionalTransformations(domain, problem, null);
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem, Function<Plan, Plan> planTransformation) {
        return planWithOptionalTransformations(domain, problem, planTransformation);
    }

    /**
     * Calculates the current score depending if we are doing temporal scheduling or sequential planning.
     *
     * @param current the current state
     * @param transformed the transformed plan, or null
     * @return the score
     */
    protected static int calculateCurrentScore(ImmutablePlanState current, Plan transformed) {
        int curScore = current.getTotalTime();
        if (transformed != null) {
            curScore = Math.round(transformed.calculateMakespan().floatValue());
        }
        return curScore;
    }

    /**
     * Plan, optionally with intermediate plan transformations.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planTransformation the plan transformation, optionally null
     * @return the plan, or an empty optional
     */
    protected Optional<Plan> planWithOptionalTransformations(Domain domain, Problem problem,
            Function<Plan, Plan> planTransformation) {
        formatLog("Initializing planning...");
        resetState();
        initialize(problem);
        formatLog("Starting planning...");

        List<Vehicle> vehicles = new ArrayList<>(problem.getAllVehicles());
        float exploration = 0.2f; // best so far: 0.2 or 0.1
        while (true) {
            ImmutablePlanState current = new ImmutablePlanState(problem);
            while (!current.isGoalState() && calculateCurrentScore(current, planTransformation == null ? null
                    : planTransformation.apply(new SequentialPlan(current.getAllActionsInList())))
                    < getBestPlanScore()) {
                Problem curProblem = current.getProblem();
                List<Package> unfinished = new ArrayList<>(
                        PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                if (unfinished.isEmpty()) {
                    List<Drive> driveToTarget = new ArrayList<>(); // will not get called for seq, used in vehicle goals
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
                Vehicle chosenVehicle = chooseVehicle(curProblem, vehicles, chosenPackage, exploration);
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
            if (planTransformation == null) {
                int curScore = calculateCurrentScore(current, null);
                if (curScore < getBestPlanScore()) {
                    savePlanIfBetter(curScore, new SequentialPlan(current.getAllActionsInList()));
                }
            } else {
                Plan curPlan = planTransformation.apply(new SequentialPlan(current.getAllActionsInList()));
                if (curPlan == null) {
                    continue;
                }
                int curScore = calculateCurrentScore(current, curPlan);
                if (curScore < getBestPlanScore()) {
                    savePlanIfBetter(Math.round((float) curScore), curPlan);
                }
            }
        }
    }

    @Override
    public RandomizedRestartAroundPathNearbyPlanner copy() {
        return new RandomizedRestartAroundPathNearbyPlanner();
    }
}
