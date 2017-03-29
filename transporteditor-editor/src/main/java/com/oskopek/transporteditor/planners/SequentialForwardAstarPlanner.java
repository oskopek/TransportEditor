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
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.parsing.combinator.testing.Str;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SequentialForwardAstarPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ImmutablePlanState, Integer> fScore;
    private Map<ImmutablePlanState, Integer> gScore;
    private Map<ImmutablePlanState, Integer> hScore;
    private Set<ImmutablePlanState> openSetContains;
    private Set<ImmutablePlanState> closedSet;
    private RoadGraph originalAPSPGraph;
    private PriorityQueue<ImmutablePlanState> openSet;
    private Plan bestPlan;
    private int bestPlanScore;

    public SequentialForwardAstarPlanner() {
        // intentionally empty
    }

    static void computeAPSP(RoadGraph graph) {
        new APSP(graph, "weight", true).compute();
    }

    private Integer getHScore(ImmutablePlanState state) {
        return hScore.computeIfAbsent(state, s -> calculateHeuristic(s, originalAPSPGraph));
    }

    private Integer getFScore(ImmutablePlanState state) {
        return fScore.getOrDefault(state, Integer.MAX_VALUE);
    }

    private Integer getGScore(ImmutablePlanState state) {
        return gScore.getOrDefault(state, Integer.MAX_VALUE);
    }

    public RoadGraph getOriginalAPSPGraph() {
        return originalAPSPGraph;
    }

    void resetState() {
        originalAPSPGraph = null;
        hScore = new HashMap<>();
        fScore = new HashMap<>();
        closedSet = new HashSet<>();
        openSet = new PriorityQueue<>(Comparator.comparing(this::getFScore));
        openSetContains = new HashSet<>();
        gScore = new HashMap<>();
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    void initialize(Domain domain, Problem problem) {
        originalAPSPGraph = (RoadGraph) Graphs.clone(problem.getRoadGraph());
        originalAPSPGraph.getAllRoads().forEach(roadEdge -> originalAPSPGraph.getEdge(roadEdge.getRoad().getName())
                .addAttribute("weight", roadEdge.getRoad().getLength().getCost()));
        computeAPSP(originalAPSPGraph);

        ImmutablePlanState start = new ImmutablePlanState(domain, problem, List.empty());
        fScore.put(start, getHScore(start));
        openSet.offer(start);
        openSetContains.add(start);
        gScore.put(start, 0);
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        resetState();
        initialize(domain, problem);
        logger.debug("Starting planning...");

        while (!openSetContains.isEmpty()) {
            ImmutablePlanState current = openSet.poll();
            openSetContains.remove(current);
//            System.out.println("\n\n" + new SequentialPlanIO(domain, problem).serialize(new SequentialPlan(current.getActions().toJavaList())));
//            logger.debug("F: {}, G: {}, H: {}", getFScore(current), getGScore(current), getHScore(current));
            if (current.isGoalState()) {
                logger.debug("Found goal state! Explored {} states. Left out {} states.", closedSet.size(),
                        openSet.size());
                if (bestPlanScore > current.getTotalTime()) {
                    logger.debug("Found new best plan {} -> {}", bestPlanScore, current.getTotalTime());
                    bestPlanScore = current.getTotalTime();
                    bestPlan = new SequentialPlan(current.getActions().toJavaList());
                }
                return Optional.of(new SequentialPlan(current.getActions().toJavaList())); // TODO: remove me?
            }

            if (shouldCancel()) {
                logger.debug("Cancelling, returning best found plan so with score: {}.", bestPlanScore);
                return Optional.ofNullable(bestPlan);
            }

            closedSet.add(current);

            Stream<Action> generatedActions = generateActions(current, current.getActions(), originalAPSPGraph);
            generatedActions.forEach(generatedAction -> {
                // Ignore the neighbor state which is already evaluated or invalid
                Optional<ImmutablePlanState> maybeNeighbor = current.apply(generatedAction)
                        .filter(state -> !closedSet.contains(state));
                if (maybeNeighbor.isPresent()) {
                    ImmutablePlanState neighbor = maybeNeighbor.get();

                    // The distance from start to a neighbor
                    int tentativeGScore = getGScore(current) + generatedAction.getDuration().getCost();

                    int neighborGScore = getGScore(neighbor);
                    boolean added = false;
                    if (!openSetContains.contains(neighbor)) {
                        openSet.offer(neighbor);
                        openSetContains.add(neighbor);
                        added = true;
                    } else if (tentativeGScore >= neighborGScore) {
//                        if (tentativeGScore > neighborGScore) {
//                            logger.debug("Try not to generate these plans"); // TODO: P22 fails.
//                        }
                        return;
                    }

                    // this path is the best until now
                    if (!added) {
                        openSet.offer(neighbor); // overwrites the correct state with shorter actions
                        // openSetContains doesn't care
                    }
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + getHScore(neighbor));
                }
            });
            if (closedSet.size() % 1_000 == 0) {
                logger.debug("Explored {} states, left: {}", closedSet.size(), openSet.size());
                logger.debug("Current plan depth: {}", current.getActions().size());
            }
        }

        return Optional.ofNullable(bestPlan);
    }

    static Integer calculateHeuristic(ImmutablePlanState state, RoadGraph apspGraph) {
        return calculateSumOfDistancesToPackageTargets(getUnfinishedPackage(state), state.getAllVehicles(), apspGraph);
    }

    static boolean hasCycle(List<Action> plannedActions) {
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
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    static java.util.List<Package> getUnfinishedPackage(ImmutablePlanState state) {
        return state.getAllPackages().stream().filter(p -> !p.getTarget().equals(p.getLocation()))
                .collect(Collectors.toList());
    }

    static boolean pickupWhereDropoff(List<Action> plannedActions) {
        java.util.Map<String, java.util.List<String>> pickedUpAt = new HashMap<>(plannedActions.size());
        plannedActions.filter(a -> a instanceof PickUp)
                .forEach(a -> pickedUpAt.computeIfAbsent(a.getWhat().getName(), s -> new ArrayList<>())
                        .add(a.getWhere().getName()));
        return !plannedActions.filter(a -> a instanceof Drop).filter(a -> pickedUpAt.getOrDefault(a.getWhat().getName(),
                Collections.emptyList()).contains(a.getWhere().getName())).isEmpty();
    }

    private static Stream<Action> generateActions(ImmutablePlanState state, List<Action> plannedActions,
            RoadGraph originalAPSPGraph) {
        if (hasCycle(plannedActions)) { // TODO: Convert to non-generation
            return Stream.empty();
        }

        Stream.Builder<Action> generated = Stream.builder();
        java.util.List<Package> packagesUnfinished = getUnfinishedPackage(state);
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
                            return generated.build();
                        }
                    }
                }
            }
        }

        Optional<Action> lastAction = plannedActions.isEmpty() ? Optional.empty()
                : Optional.of(plannedActions.get(plannedActions.size() - 1));
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
            Map<String, java.util.List<String>> vehicleDroppedAfterLastMove = getPackagesDroppedAfterLastMoveMap(plannedActions);
            packageMap.keySet().forEach(location -> {
                List<Package> packages = packageMap.get(location);
                List<Vehicle> vehiclesAtLoc = vehicleMap.get(location);
                if (packages == null || vehiclesAtLoc == null) {
                    return;
                }

                for (Vehicle vehicle : vehiclesAtLoc) {
                    java.util.List<String> droppedNames = vehicleDroppedAfterLastMove.get(vehicle.getName());
                    for (Package pkg : packages) {
                        if (droppedNames != null && droppedNames.contains(pkg.getName())) {
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
            generateDrivesForVehicle(vehicle, graph, domain, originalAPSPGraph, plannedActions).forEach(generated::add);
        } else {
            for (Vehicle vehicle : vehicles) {
                generateDrivesForVehicle(vehicle, graph, domain, originalAPSPGraph, plannedActions).forEach(generated::add);
            }
        }

        return generated.build();
    }

    // Vehicle -> [Package]
    private static Map<String, java.util.List<String>> getPackagesDroppedAfterLastMoveMap(List<Action> plannedActions) {
        // Vehicle -> int (index into plannedActions)
        Map<String, Integer> lastDriveIndexMap = new HashMap<>();
        for (int i = 0; i < plannedActions.size(); i++) {
            Action action = plannedActions.get(i);
            Drive drive;
            if (!(action instanceof Drive)) {
                continue;
            }
            drive = (Drive) action;
            lastDriveIndexMap.put(drive.getWho().getName(), i);
        }

        Map<String, java.util.List<String>> packagesDroppedAfterLastMoveMap = new HashMap<>(lastDriveIndexMap.size());
        for (Map.Entry<String, Integer> entry : lastDriveIndexMap.entrySet()) {
            String vehicleName = entry.getKey();
            int lastDriveIndex = entry.getValue();

            for (int i = lastDriveIndex + 1; i < plannedActions.size(); i++) {
                Action action = plannedActions.get(i);
                if (action instanceof Drop && action.getWho().getName().equals(vehicleName)) {
                    packagesDroppedAfterLastMoveMap.computeIfAbsent(vehicleName, v -> new ArrayList<>()).add(action.getWhat().getName());
                }
            }
        }
        return packagesDroppedAfterLastMoveMap;
    }

    private static java.util.List<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain, // TODO: to stream
            RoadGraph originalAPSPGraph, List<Action> plannedActions) {
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

    static boolean doesShorterPathExist(Vehicle vehicle, Location target, List<Action> plannedActions,
            RoadGraph apspGraph) {
        if (plannedActions.isEmpty()) {
            return false;
        } // TODO: memoize

        Drive lastDrive = null;
        int lengthOfPath = 0;
        Location sourceOfPreviousDrives;

        for (int i = plannedActions.size() - 1; i >= 0; i--) {
            Action plannedAction = plannedActions.get(i);
            if (!plannedAction.getWho().getName().equals(vehicle.getName())) {
                continue;
            }
            if (plannedAction instanceof Drive) {
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

        return getLengthToCorrect(apspGraph.getNode(sourceOfPreviousDrives.getName())
                .getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME), target) <= lengthOfPath;
    }

    static int calculateSumOfDistancesToPackageTargets(java.util.Collection<Package> packageList,
            java.util.Collection<Vehicle> vehicleList, RoadGraph graph) {
        int sumDistances = 0;
        for (Vehicle vehicle : vehicleList) { // vehicles are never in the middle of a drive
            APSP.APSPInfo current = graph.getNode(vehicle.getLocation().getName()).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (Package pkg : vehicle.getPackageList()) {
                sumDistances += getLengthToCorrect(current, pkg.getTarget()) + 1; // + drop action
            }
        }
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                sumDistances += getLengthToCorrect(graph.getNode(pkgLocation.getName())
                        .getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME), pkg.getTarget()) + 2; // + pickup and drop
            }
        }
        return sumDistances;
    }

    public static double getLengthToCorrect(APSP.APSPInfo current, Location target) {
        String targetName = target.getName();
        if (current.getNodeId().equals(targetName)) { // fix weird behavior of APSP
            return 0d;
        } else {
            return current.getLengthTo(targetName);
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
