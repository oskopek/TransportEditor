package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.Tuple;
import javaslang.Tuple3;
import javaslang.Value;
import javaslang.collection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// choose a random vehicle and package,
// find the path, find all packages on it
// choose the ones whose target is on the path
public class RandomizedRestartWithOnPathPickupPlanner extends SequentialRandomizedPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RandomizedRestartWithOnPathPickupPlanner() {
        setName(RandomizedRestartWithOnPathPickupPlanner.class.getSimpleName());
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
                List<Package> unfinished = new ArrayList<>(PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
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

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage, unfinished);
                current = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to current state."));

                if (shouldCancel()) {
                    logger.debug("Cancelling, returning best found plan so far with score: {}.", getBestPlanScore());
                    return Optional.ofNullable(getBestPlan());
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Finished one iteration. Length: {}", current.getTotalTime());
            }

            // TODO: collapse plan?
            if (getBestPlanScore() > current.getTotalTime()) {
                logger.debug("Found new best plan {} -> {}", getBestPlanScore(), current.getTotalTime());
                setBestPlanScore(current.getTotalTime());
                setBestPlan(new SequentialPlan(current.getAllActionsInList()));
            }
        }
    }

    protected List<Action> findPartialPlan(Domain domain, ImmutablePlanState current, String chosenVehicleName,
            final Package chosenPackage, List<Package> unfinished) {
        Map<Location, Set<Package>> packageLocMap = new HashMap<>();
        for (Package pkg : unfinished) {
            packageLocMap.computeIfAbsent(pkg.getLocation(), l -> new HashSet<>()).add(pkg);
        }

        final Vehicle chosenVehicle = current.getProblem().getVehicle(chosenVehicleName);
        Location packageLoc = chosenPackage.getLocation();
        List<RoadEdge> basicPath = new ArrayList<>();
        basicPath.addAll(getShortestPathMatrix().get(chosenVehicle.getLocation().getName(), packageLoc.getName()).getRoads());
        basicPath.addAll(getShortestPathMatrix().get(packageLoc.getName(), chosenPackage.getTarget().getName()).getRoads());
        if (basicPath.isEmpty()) {
            return Collections.emptyList();
        }
        List<Location> locationsOnPath = basicPath.stream().map(RoadEdge::getFrom).collect(Collectors.toList());
        locationsOnPath.add(basicPath.get(basicPath.size() - 1).getTo());

        Set<Package> packagesOnPath = new HashSet<>();
        for (RoadEdge edge : basicPath) {
            Set<Package> atFrom = packageLocMap.get(edge.getFrom());
            if (atFrom != null) {
                packagesOnPath.addAll(atFrom);
            }
        }

        javaslang.collection.List<Tuple3<Integer, Integer, Package>> completelyOnPath = Stream.ofAll(packagesOnPath)
                .map(p -> Tuple.of(locationsOnPath.indexOf(p.getLocation()), locationsOnPath.lastIndexOf(p.getTarget()), p))
                .filter(t -> t._1 >= 0 && t._2 >= 0 && t._1 <= t._2).toList();
        // only packages with pick up and drop on path and with pick up before drop
        // list should have at least one entry now
        final int curCapacity = chosenVehicle.getCurCapacity().getCost();
        List<Package> chosenPackages;
        if ((long) completelyOnPath.map(t -> t._3.getSize().getCost()).sum() > curCapacity) { // if not take everything
            chosenPackages = Stream.ofAll(completelyOnPath).combinations().map(Value::toList)
                    .map(sTuple -> Tuple.of(curCapacity - PlannerUtils.calculateMaxCapacity(sTuple), sTuple.map(t -> t._3)))
                    .filter(t -> t._1 >= 0).map(t -> Tuple.of(t._1, t._2.toStream().map(p -> locationsOnPath.lastIndexOf(p.getTarget())).max().getOrElse(Integer.MAX_VALUE), t._2))
                    .minBy(t -> Tuple.of(t._1, t._2)) // TODO: T2 not last index, but length of indexes + maximize?
                    .map(t -> t._3.toJavaList()).getOrElse((List<Package>) null);
        } else {
            chosenPackages = completelyOnPath.map(t -> t._3).toJavaList();
        }

        if (chosenPackages == null) {
            throw new IllegalStateException("Should not occur.");
        }

        List<RoadEdge> path = basicPath;
        return PlannerUtils.buildPlan(domain, path, chosenVehicle, chosenPackages, Collections.emptyMap());
    }

    @Override
    public RandomizedRestartWithOnPathPickupPlanner copy() {
        return new RandomizedRestartWithOnPathPickupPlanner();
    }
}
