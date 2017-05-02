package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.PlannerUtils;
import com.oskopek.transport.planners.sequential.RandomizedRestartAroundPathNearbyPlanner;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.collection.Stream;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Scheduler wrapper around {@link TemporalRandomizedRestartAroundPathNearbyPlanner}.
 */
public class TRRAPNSequentialScheduler extends SequentialScheduler {

    private TemporalRandomizedRestartAroundPathNearbyPlanner planner;

    @Override
    public Optional<Plan> plan(Domain domain, Problem tempProblem) {
        planner = new TemporalRandomizedRestartAroundPathNearbyPlanner(tempProblem);
        return super.plan(domain, tempProblem);
    }

    @Override
    public Optional<Plan> plan(Domain seqDomain, Problem seqProblem,
            Function<Plan, Plan> planTransformation) {
        return planner.plan(seqDomain, seqProblem, planTransformation);
    }

    @Override
    protected AbstractPlanner getInternalPlanner() {
        return planner;
    }

    @Override
    public TRRAPNSequentialScheduler copy() {
        return new TRRAPNSequentialScheduler();
    }

    /**
     * Chooses a package randomly and choose a nearby vehicle or a random one,
     * based on a biased coin flip.
     * Find the path to deliver the package by the vehicle,
     * find all packages on it and around it
     * choose the ones whose target is on the path
     * if still not fully capacitated, choose the others in the
     * order of distance from their destinations at any possible drop of point on the path.
     * <p>
     * With an exponentially increasing probability for every N unsuccessful plan proposals,
     * instead of picking up a package, go to the nearest (actually sampled from a distance distribution)
     * petrol station (and possibly refuel there).
     */
    private static class TemporalRandomizedRestartAroundPathNearbyPlanner
            extends RandomizedRestartAroundPathNearbyPlanner {

        private Problem tempProblem;

        /**
         * Default constructor.
         */
        public TemporalRandomizedRestartAroundPathNearbyPlanner(Problem tempProblem) {
            setName(TemporalRandomizedRestartAroundPathNearbyPlanner.class.getSimpleName());
            this.tempProblem = tempProblem;
            logger = LoggerFactory.getLogger(getClass());
        }

        /**
         * Plan, optionally with intermediate plan transformations.
         *
         * @param domain the domain
         * @param problem the problem
         * @param planTransformation the plan transformation, optionally null
         * @return the plan, or an empty optional
         */
        @Override
        protected Optional<Plan> planWithOptionalTransformations(Domain domain, Problem problem,
                Function<Plan, Plan> planTransformation) {
            formatLog("Initializing planning...");
            resetState();
            initialize(problem);
            formatLog("Starting planning...");

            List<Vehicle> vehicles = new ArrayList<>(problem.getAllVehicles());
            List<Location> petrolStations = tempProblem.getRoadGraph().getAllLocations()
                    .filter(Location::hasPetrolStation).collect(Collectors.toList());
            float exploration = 0.2f; // best so far: 0.2 or 0.1
            float temperature = 0.05f;
            long stepsSinceBest = 0L;
            float maxFuel = 0.5f;
            float minFuel = 0.000003f;
            float stepFuel = 2f;
            float curFuel = minFuel;
            while (true) {
                stepsSinceBest++;
                if (stepsSinceBest % 1_000 == 0) {
                    curFuel *= stepFuel;
                    if (curFuel > maxFuel) {
                        curFuel = maxFuel;
                    }
                    if (curFuel != maxFuel) {
                        logger.debug("Setting refuel probability to {}.", curFuel);
                    }
                }

                ImmutablePlanState current = new ImmutablePlanState(problem);
                while (!current.isGoalState() && calculateCurrentScore(current, planTransformation == null ? null
                        : planTransformation.apply(new SequentialPlan(current.getAllActionsInList())))
                        < getBestPlanScore()) {
                    Problem curProblem = current.getProblem();
                    List<Package> unfinished = new ArrayList<>(
                            PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                    if (unfinished.isEmpty()) {  // will not get called for seq, used in vehicle goals
                        List<Drive> driveToTarget = new ArrayList<>();
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

                    List<Action> newActions;
                    if (getRandom().nextFloat() < curFuel) {
                        Vehicle chosen = curProblem.getVehicle(vehicles.get(getRandom().nextInt(vehicles.size()))
                                .getName());
                        Location nearestPetrolStation = chooseFromDistanceDistribution(Stream.ofAll(petrolStations),
                                chosen.getLocation(), temperature, true);
                        newActions = getShortestPathMatrix().get(chosen.getLocation().getName(),
                                nearestPetrolStation.getName()).getRoads().stream().map(r -> domain
                                .buildDrive(chosen, r.getFrom(), r.getTo(), r.getRoad())).collect(Collectors.toList());
                    } else {
                        Package chosenPackage = unfinished.get(getRandom().nextInt(unfinished.size()));
                        Vehicle chosenVehicle = chooseVehicle(curProblem, vehicles, chosenPackage, exploration);
                        newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                                unfinished, true);
                    }

                    current = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                            (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                            .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to state."));

                    if (shouldCancel()) {
                        formatLog("Cancelling, returning best found plan so far with score: {}.",
                                getBestPlanScore());
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
                        stepsSinceBest = 0L;
                        savePlanIfBetter(curScore, new SequentialPlan(current.getAllActionsInList()));
                    }
                } else {
                    Plan curPlan = planTransformation.apply(new SequentialPlan(current.getAllActionsInList()));
                    if (curPlan == null) {
                        continue;
                    }
                    int curScore = calculateCurrentScore(current, curPlan);
                    if (curScore < getBestPlanScore()) {
                        stepsSinceBest = 0L;
                        savePlanIfBetter(Math.round((float) curScore), curPlan);
                    }
                }
            }
        }

        @Override
        public TemporalRandomizedRestartAroundPathNearbyPlanner copy() {
            return new TemporalRandomizedRestartAroundPathNearbyPlanner(tempProblem);
        }
    }

}
