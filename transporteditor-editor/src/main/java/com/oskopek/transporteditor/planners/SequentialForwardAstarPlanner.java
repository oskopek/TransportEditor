package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.Drop;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.state.ImmutablePlanState;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SequentialForwardAstarPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SequentialForwardAstarPlanner() {
        // intentionally empty
    }

    private static void computeAPSP(RoadGraph graph) {
        new APSP(graph, "weight", true).compute();
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        final RoadGraph originalAPSPGraph = (RoadGraph) Graphs.clone(problem.getRoadGraph());
        originalAPSPGraph.getAllRoads().forEach(roadEdge -> originalAPSPGraph.getEdge(roadEdge.getRoad().getName()).addAttribute("weight", roadEdge.getRoad().getLength().getCost()));
        computeAPSP(originalAPSPGraph);

        Map<ImmutablePlanState, Integer> hScore = new HashMap<>();
        Function<ImmutablePlanState, Integer> hScoreGetter = immutablePlanState -> hScore
                .computeIfAbsent(immutablePlanState, s -> calculateHeuristic(s, originalAPSPGraph));

        ImmutablePlanState start = new ImmutablePlanState(domain, problem, List.empty());

        Map<ImmutablePlanState, Integer> fScore = new HashMap<>();
        Function<ImmutablePlanState, Integer> fScoreGetter = immutablePlanState ->
                fScore.getOrDefault(immutablePlanState, Integer.MAX_VALUE);
        fScore.put(start, hScoreGetter.apply(start));

        Set<ImmutablePlanState> closedSet = new HashSet<>();
        PriorityQueue<ImmutablePlanState> openSet = new PriorityQueue<>(Comparator.comparing(s -> fScoreGetter.apply(s)));
        openSet.add(start);

        Map<ImmutablePlanState, Integer> gScore = new HashMap<>();
        Function<ImmutablePlanState, Integer> gScoreGetter = immutablePlanState ->
                gScore.getOrDefault(immutablePlanState, Integer.MAX_VALUE);
        gScore.put(start, 0);
        logger.debug("Starting planning...");

        int counter = 0;
        while (!openSet.isEmpty()) {
            ImmutablePlanState current = openSet.poll();
            if (current.isGoalState()) {
                logger.debug("Found goal state! Exiting. Explored {} states. Left out {} states.", closedSet.size(),
                        openSet.size());
                return Optional.of(new SequentialPlan(current.getActions().toJavaList()));
            }

            if (shouldCancel()) {
                logger.debug("Cancelling, returning empty plan.");
                return Optional.empty();
            }

            closedSet.add(current);

            java.util.List<Action> generatedActions = generateActions(current, current.getActions(), originalAPSPGraph);
            java.util.List<ImmutablePlanState> generatedStates = new ArrayList<>(generatedActions.size());
            for (Action generatedAction : generatedActions) {
                // Ignore the neighbor state which is already evaluated or invalid
                Optional<ImmutablePlanState> maybeNeighbor = current.apply(generatedAction).filter(state -> !closedSet.contains(state));
                if (maybeNeighbor.isPresent()) {
                    ImmutablePlanState neighbor = maybeNeighbor.get();
                    if (closedSet.contains(neighbor)) {
                        throw new IllegalStateException("Should not occur.");
                    }

                    // The distance from start to a neighbor
                    int tentativeGScore = gScoreGetter.apply(current) + generatedAction.getDuration().getCost();

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else if (tentativeGScore >= gScoreGetter.apply(neighbor)) {
//                        verifyActionsOfOptimalP02Plan(neighbor.getActions()); // TODO: remove me
                        System.out.println("Try not to generate these plans"); // TODO : successive drops? // TODO: P22 fails.
                        continue;
                    }

                    // this path is the best until now
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, gScoreGetter.apply(neighbor) + hScoreGetter.apply(neighbor));
                }
            }

            counter++;
            if (counter % 1_000 == 0) {
                logger.debug("Explored {} ({}) states, left: {}", counter, closedSet.size(), openSet.size());
                logger.debug("Current plan depth: {}", current.getActions().size());
            }
        }

        return Optional.empty();
    }

    private static Integer calculateHeuristic(ImmutablePlanState state, RoadGraph apspGraph) {
        return calculateSumOfDistancesToPackageTargets(getUnfinishedPackage(state), apspGraph);
    }

    private static java.util.Set<Tuple2<String, String>> didDropThisJustNow(List<Action> plannedActions) {
        java.util.Set<Tuple2<String, String>> drops = new HashSet<>();
        int index;
        for (index = plannedActions.size() - 1; index >= 0; index--) {
            Action plannedAction = plannedActions.get(index);
            if (plannedAction instanceof Drop) {
                drops.add(Tuple.of(plannedAction.getWho().getName(), plannedAction.getWhat().getName()));
            } else if (plannedAction instanceof PickUp) {
                continue;
            }
            break;
        }
        return drops;
    }

    private static boolean hasCycle(List<Action> plannedActions) {
        if (plannedActions.size() < 2) {
            return false;
        }
        java.util.Set<String> drives = new HashSet<>();
        int lastActionIndex = plannedActions.size() - 1;
        Action lastAction = plannedActions.get(lastActionIndex);
        if (lastAction instanceof Drive) { // add last target
            drives.add(lastAction.getWhat().getName());
        }
        for (int index = lastActionIndex; index >= 0; index--) {
            Action plannedAction = plannedActions.get(index);
            if (plannedAction instanceof Drive) {
                if (!drives.add(plannedAction.getWhere().getName())) {
                    // CYCLE!
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    private static java.util.List<Package> getUnfinishedPackage(ImmutablePlanState state) {
        return state.getAllPackages().stream().filter(p -> !p.getTarget().equals(p.getLocation())).collect(Collectors.toList());
    }

    private static boolean pickupWhereDropoff(List<Action> plannedActions) {
        java.util.Map<String, java.util.List<String>> pickedUpAt = new HashMap<>(plannedActions.size());
        plannedActions.filter(a -> a instanceof PickUp)
                .forEach(a -> pickedUpAt.computeIfAbsent(a.getWhat().getName(), s -> new ArrayList<>()).add(a.getWhere().getName()));
        return !plannedActions.filter(a -> a instanceof Drop).filter(a -> pickedUpAt.getOrDefault(a.getWhat().getName(),
                Collections.emptyList()).contains(a.getWhere().getName())).isEmpty();
    }

    private static java.util.List<Action> generateActions(ImmutablePlanState state, List<Action> plannedActions, RoadGraph originalAPSPGraph) {
        if (hasCycle(plannedActions)) { // TODO: Convert to non-generation
            return Collections.emptyList();
        }

        java.util.List<Action> generated = new ArrayList<>();
        java.util.List<Package> packagesUnfinished = getUnfinishedPackage(state);
//        java.util.List<Vehicle> vehicles = Collections.singletonList(state.getVehicle("truck-2"));
        java.util.List<Vehicle> vehicles = new ArrayList<>(state.getAllVehicles());

        Domain domain = state.getDomain();
        RoadGraph graph = state.getRoadGraph();

        Map<Location, List<Vehicle>> vehicleMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            Location current = vehicle.getLocation();
            vehicleMap.put(current, vehicleMap.getOrDefault(current, List.empty()).append(vehicle));
        }
        Map<Location, List<Package>> packageMap = new HashMap<>();
        for (Package pkg : packagesUnfinished) {
            Location current = pkg.getLocation();
            if (current != null) {
                packageMap.put(current, packageMap.getOrDefault(current, List.empty()).append(pkg));
            }
        }

        // drop at target above all else
        for (Package pkg : packagesUnfinished) {
            if (pkg.getLocation() == null) { // unfinished package is in vehicle
                Location target = pkg.getTarget();
                List<Vehicle> vehiclesAtLoc = vehicleMap.get(target);
                if (vehiclesAtLoc != null) {
                    for (Vehicle vehicle : vehiclesAtLoc) { // vehicles at target
                        if (vehicle.getPackageList().contains(pkg)) {
                            generated.add(domain.buildDrop(vehicle, target, pkg));
                            return generated;
                        }
                    }
                }
            }
        }

        Optional<Action> lastAction = plannedActions.isEmpty() ? Optional.empty() : Optional.of(plannedActions.get(plannedActions.size() - 1));
        // pick-up
        Optional<Vehicle> lastVehicleAndLastDrive = lastAction
                .filter(a -> a instanceof Drive).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getVehicle(v.getName()));
        if (lastVehicleAndLastDrive.isPresent()) { // only use active vehicle
            Vehicle vehicle = lastVehicleAndLastDrive.get();
            Location location = vehicle.getLocation();

            if (!location.equals(plannedActions.last().getWhat())) {
                throw new IllegalStateException("Planner assumptions broken.");
            }
            List<Package> packages = packageMap.get(location);
            if (packages != null) {
                for (Package pkg : packages) {
                    if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                        generated.add(domain.buildPickUp(vehicle, location, pkg));
                    }
                }
            }
        } else {
            java.util.Set<Tuple2<String, String>> dropped = didDropThisJustNow(plannedActions);
            packageMap.keySet().forEach(location -> {
                List<Package> packages = packageMap.get(location);
                List<Vehicle> vehiclesAtLoc = vehicleMap.get(location);
                if (packages == null || vehiclesAtLoc == null) {
                    return;
                }

                for (Package pkg : packages) {
                    for (Vehicle vehicle : vehiclesAtLoc) {
                        if (dropped.contains(Tuple.of(vehicle.getName(), pkg.getName()))) {
                            continue;
                        }
                        if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                            generated.add(domain.buildPickUp(vehicle, location, pkg));
                        }
                    }
                }
            });
        }

        // drop

        if (lastAction.filter(a -> !(a instanceof PickUp)).isPresent()) { // do not drop after pick up
            if (lastVehicleAndLastDrive.isPresent()) { // only drop from active vehicle
                Vehicle vehicle = lastVehicleAndLastDrive.get();
                Location current = vehicle.getLocation();
                for (Package pkg : vehicle.getPackageList()) {
                    Drop drop = domain.buildDrop(vehicle, current, pkg);
                    if (pickupWhereDropoff(plannedActions.append(drop))) { // TODO: make quicker
                        continue;
                    }
                    generated.add(drop);
                }
            } else {
                for (Vehicle vehicle : vehicles) {
                    Location current = vehicle.getLocation();
                    for (Package pkg : vehicle.getPackageList()) {
                        Drop drop = domain.buildDrop(vehicle, current, pkg);
                        if (pickupWhereDropoff(plannedActions.append(drop))) { // TODO: make quicker
                            continue;
                        }
                        generated.add(drop);
                    }
                }
            }
        }

        // drive
        Optional<Vehicle> lastVehicleAndNotDrop = lastAction
                .filter(a -> !(a instanceof Drop)).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getVehicle(v.getName()));

        if (lastVehicleAndNotDrop.isPresent()) { // continue driving if driving
            Vehicle vehicle = lastVehicleAndNotDrop.get();
            generated.addAll(generateDrivesForVehicle(vehicle, graph, domain, originalAPSPGraph, plannedActions));
        } else {
            for (Vehicle vehicle : vehicles) {
                generated.addAll(generateDrivesForVehicle(vehicle, graph, domain, originalAPSPGraph, plannedActions));
            }
        }

        return generated;
    }

    private static boolean doesShorterPathExist(Vehicle vehicle, Location target, List<Action> plannedActions, RoadGraph apspGraph) {
        if (plannedActions.isEmpty()) {
            return false;
        } // TODO: memoize this and check if it works correctly

        Drive lastDrive = null;
        int lengthOfPath = 0;
        Location sourceOfPreviousDrives = null;
        Location targetOfPreviousDrives = null;

        for (int i = plannedActions.size() - 1; i >= 0; i--) {
            Action plannedAction = plannedActions.get(i);
            if (plannedAction instanceof Drive && plannedAction.getWho().getName().equals(vehicle.getName())) {
                if (lastDrive == null) { // last drive
                    targetOfPreviousDrives = (Location) plannedAction.getWhat();
                }
                lastDrive = (Drive) plannedAction;
                lengthOfPath += lastDrive.getDuration().getCost();
                continue;
            }
            break;
        }

        if (lastDrive == null) {
            return false; // no drives
        } else {
            sourceOfPreviousDrives = lastDrive.getWhere();
        }

        lengthOfPath += (int) apspGraph.getNode(targetOfPreviousDrives.getName()).getEdgeToward(target.getName()).getAttribute("weight");

        APSP.APSPInfo info = apspGraph.getNode(sourceOfPreviousDrives.getName()).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
        if (info.getLengthTo(target.getName()) <= lengthOfPath) {
            return true; // shorter path exists
        }
        return false;
    }

    private static java.util.List<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain, RoadGraph originalAPSPGraph, List<Action> plannedActions) {
        java.util.List<Drive> vehicleActions = new ArrayList<>();
        Location current = vehicle.getLocation();
        for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
            Location target = graph.getLocation(edge.getTargetNode().getId());
            if (doesShorterPathExist(vehicle, target, plannedActions, originalAPSPGraph)) {
                continue;
            }
            vehicleActions.add(domain.buildDrive(vehicle, current, target, graph.getRoad(edge.getId())));
        }
//        if (!vehicle.getPackageList().isEmpty()) { // TODO: doesn't work well globally
//            Map<Location, Integer> sumOfDistancesToPackageTargets = calculateSumOfDistancesToPackageTargetsMap(
//                    vehicle.getPackageList(), originalAPSPGraph);
//            vehicleActions.sort(Comparator.comparing(d -> sumOfDistancesToPackageTargets.get(d.getWhat())));
//        }
        return vehicleActions;
    }

    private static Map<Location, Integer> calculateSumOfDistancesToPackageTargetsMap(java.util.List<Package> packageList, RoadGraph graph) {
        Map<Location, Integer> map = new HashMap<>(graph.getNodeCount());
        graph.getAllLocations().forEach(location -> {
            double sum = 0;
            APSP.APSPInfo info = graph.getNode(location.getName()).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (Package pkg : packageList) {
                Location target = pkg.getTarget();
                double distance = 0d;
                if (!target.getName().equals(location.getName())) { // fix weird behavior of APSP in GraphStream
                    distance = info.getLengthTo(target.getName());
                }
                sum += distance;
            }
            map.put(location, (int) sum);
        });
        return map;
    }

    private static Integer calculateSumOfDistancesToPackageTargets(java.util.List<Package> packageList, RoadGraph graph) {
        int sum = 0;
        for (Location location : graph.getAllLocations().collect(Collectors.toList())) {
            APSP.APSPInfo info = graph.getNode(location.getName()).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (Package pkg : packageList) {
                Location target = pkg.getTarget();
                double distance = 0d;
                if (!target.getName().equals(location.getName())) { // fix weird behavior of APSP in GraphStream
                    distance = info.getLengthTo(target.getName());
                }
                sum += (int) distance;
            }
        }
        return sum;
    }

    private static List<String> optimalActions = List.of("pick-up",
            "drive",
            "drop",
            "pick-up",
            "drive",
            "drive",
            "drive",
            "pick-up",
            "drive",
            "drop",
            "drive",
            "drop",
            "drive",
            "pick-up",
            "drive",
            "drive",
            "drive",
            "drive",
            "drop");

    private static void verifyActionsOfOptimalP02Plan(List<Action> actions) {
        if (actions.map(Action::getName).zip(optimalActions).filter(t -> !t._1.equals(t._2)).isEmpty()) {
            if (actions.size() > 1 && actions.get(0).getWho().getName().equals("truck-2")) {
                System.out.println("Found optimal plan");
            }
        }
    }

    @Override
    public String getName() {
        return SequentialForwardAstarPlanner.class.getName();
    }

    @Override
    public SequentialForwardAstarPlanner copy() {
        return new SequentialForwardAstarPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SequentialForwardAstarPlanner;
    }
}
