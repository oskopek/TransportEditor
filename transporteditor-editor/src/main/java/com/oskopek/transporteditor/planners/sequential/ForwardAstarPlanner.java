package com.oskopek.transporteditor.planners.sequential;

import com.google.common.collect.ArrayTable;
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
import com.oskopek.transporteditor.model.state.ProblemPlanningWrapper;
import com.oskopek.transporteditor.planners.AbstractPlanner;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teneighty.heap.AbstractHeap;
import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForwardAstarPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ProblemPlanningWrapper, Integer> fScore;
    private Map<ProblemPlanningWrapper, Integer> gScore;
    private Map<ProblemPlanningWrapper, Integer> hScore;
    private Map<ImmutablePlanState, Heap.Entry<Integer, ImmutablePlanState>> entryMap;
    private Set<ProblemPlanningWrapper> closedSet;
    private AbstractHeap<Integer, ImmutablePlanState> openSet;
    private ObjectProperty<ArrayTable<String, String, Integer>> distanceMatrix = new SimpleObjectProperty<>();
    private Plan bestPlan;
    private int bestPlanScore;

    public ForwardAstarPlanner() {
        setName(ForwardAstarPlanner.class.getSimpleName());
    }

    static void computeAPSP(RoadGraph graph) {
        new APSP(graph, "weight", true).compute();

    }

    private Integer getHScore(ImmutablePlanState state) {
        return hScore.computeIfAbsent(state,
                s -> calculateHeuristic(s, distanceMatrix.get(), getUnfinishedPackages(s.getAllPackages())));
    }

    private Integer getFScore(ImmutablePlanState state) {
        return fScore.getOrDefault(state, Integer.MAX_VALUE);
    }

    private Integer getGScore(ImmutablePlanState state) {
        return gScore.getOrDefault(state, Integer.MAX_VALUE);
    }

    public ArrayTable<String, String, Integer> getDistanceMatrix() {
        return distanceMatrix.get();
    }

    void resetState() {
        hScore = new HashMap<>();
        fScore = new HashMap<>();
        closedSet = new HashSet<>();
        openSet = new BinaryHeap<>();
        entryMap = new HashMap<>();
        gScore = new HashMap<>();
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    void initialize(Domain domain, Problem problem) {
        RoadGraph originalAPSPGraph = (RoadGraph) Graphs.clone(problem.getRoadGraph());
        originalAPSPGraph.getAllRoads().forEach(roadEdge -> originalAPSPGraph.getEdge(roadEdge.getRoad().getName())
                .addAttribute("weight", roadEdge.getRoad().getLength().getCost()));
        computeAPSP(originalAPSPGraph);
        List<String> locationNames = originalAPSPGraph.getNodeSet().stream().map(Element::getId).collect(
                Collectors.toList());
        ArrayTable<String, String, Integer> distanceMatrix = ArrayTable.create(locationNames, locationNames);
        for (String from : locationNames) {
            APSP.APSPInfo current = originalAPSPGraph.getNode(from).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (String to : locationNames) {
                if (null != distanceMatrix.put(from, to, (int) getLengthToCorrect(current, to))) {
                    throw new IllegalStateException("Overwritten a value.");
                }
            }
        }
        this.distanceMatrix.setValue(distanceMatrix);

        ImmutablePlanState start = new ImmutablePlanState(domain, problem, Collections.emptyList());
        int startHScore = getHScore(start);
        fScore.put(start, startHScore);
        entryMap.put(start, openSet.insert(startHScore, start));
        gScore.put(start, 0);
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(domain, problem);
        logger.debug("Starting planning...");

        while (!entryMap.isEmpty()) {
            ImmutablePlanState current = openSet.extractMinimum().getValue();
            entryMap.remove(current);
//            System.out.println("\n\n" + new SequentialPlanIO(domain, problem).serialize(new SequentialPlan(current
// .getActions().toJavaList())));
//            logger.debug("F: {}, G: {}, H: {}", getFScore(current), getGScore(current), getHScore(current));
            if (current.isGoalState()) {
//                logger.debug("Found goal state! Explored {} states. Left out {} states.", closedSet.size(),
//                        openSet.getKeys().size());
                if (bestPlanScore > current.getTotalTime()) {
                    logger.debug("Found new best plan {} -> {}", bestPlanScore, current.getTotalTime());
                    bestPlanScore = current.getTotalTime();
                    bestPlan = new SequentialPlan(current.getActions());
                }
//                return Optional.of(new SequentialPlan(current.getActions().toJavaList())); // TODO: remove me?
            }

            if (shouldCancel()) {
                logger.debug("Cancelling, returning best found plan so with score: {}.", bestPlanScore);
                return Optional.ofNullable(bestPlan);
            }

            closedSet.add(new ProblemPlanningWrapper(current));

            Stream<Action> generatedActions = generateActions(current, current.getActions(), distanceMatrix.get(),
                    getUnfinishedPackages(current.getAllPackages()));
            generatedActions.forEach(generatedAction -> {
                // Ignore the neighbor state which is already evaluated or invalid
                Optional<ImmutablePlanState> maybeNeighbor = current.apply(generatedAction)
                        .filter(state -> !closedSet.contains(state));
                if (maybeNeighbor.isPresent()) {
                    ImmutablePlanState neighbor = maybeNeighbor.get();

                    // The distance from start to a neighbor
                    int tentativeGScore = getGScore(current) + generatedAction.getDuration().getCost();
                    int neighborFScore = tentativeGScore + getHScore(neighbor);
                    int neighborGScore = getGScore(neighbor);

                    Heap.Entry<Integer, ImmutablePlanState> neighborEntry = entryMap.get(neighbor);
                    if (neighborEntry == null) {
                        neighborEntry = openSet.insert(neighborFScore, neighbor);
                        entryMap.put(neighbor, neighborEntry);
                    } else if (tentativeGScore >= neighborGScore) {
//                        if (tentativeGScore > neighborGScore) {
//                            logger.debug("Try not to generate these plans"); // TODO: P22 nonopt, p03 nonopt, p04
// nonopt?
//                        }
                        return;
                    }

                    // this path is the best until now
                    openSet.decreaseKey(neighborEntry,
                            neighborFScore); // TODO check if overwrites the correct state with shorter actions
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, neighborFScore);
                }
            });
            if (closedSet.size() % 100_000 == 0) {
                logger.debug("Explored {} states, left: {} ({})", closedSet.size(), openSet.getEntries().size(),
                        entryMap.size());
                logger.debug("Current plan depth: {}", current.getActions().size());
            }
        }

        return Optional.ofNullable(bestPlan);
    }

    static Integer calculateHeuristic(ProblemPlanningWrapper state, ArrayTable<String, String, Integer> distanceMatrix,
            Collection<Package> unfinishedPackages) {
        return calculateSumOfDistancesToPackageTargets(unfinishedPackages, state.getAllVehicles(), distanceMatrix);
    }

    static boolean hasCycle(List<Action> plannedActions) {
        if (plannedActions.size() < 2) {
            return false;
        }
        Set<String> drives = new HashSet<>();
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

    static Set<Package> getUnfinishedPackages(Collection<Package> packages) {
        Set<Package> unfinishedPackages = new HashSet<>(packages.size());
        for (Package pkg : packages) {
            if (pkg.getLocation() == null) {
                unfinishedPackages.add(pkg);
            } else if (!pkg.getLocation().getName().equals(pkg.getTarget().getName())) {
                unfinishedPackages.add(pkg);
            }
        }
        return unfinishedPackages;
    }

    static boolean pickupWhereDropoff(List<Action> plannedActions, Action newAction) {
        Map<String, Set<String>> pickedUpAt = new HashMap<>(plannedActions.size() + 1);
        for (Action a : plannedActions) {
            if (a instanceof PickUp) {
                pickedUpAt.computeIfAbsent(a.getWhat().getName(), s -> new HashSet<>())
                        .add(a.getWhere().getName());
            }
        }
        if (newAction instanceof PickUp) {
            pickedUpAt.computeIfAbsent(newAction.getWhat().getName(), s -> new HashSet<>())
                    .add(newAction.getWhere().getName());
        } else if (newAction instanceof Drop) {
            Set<String> pickedBySameCar = pickedUpAt.get(newAction.getWhat().getName());
            if (pickedBySameCar != null && pickedBySameCar.contains(newAction.getWhere().getName())) {
                return true;
            }
        }

        for (Action a : plannedActions) {
            if (a instanceof Drop) {
                Set<String> pickedBySameCar = pickedUpAt.get(newAction.getWhat().getName());
                if (pickedBySameCar != null && pickedBySameCar.contains(newAction.getWhere().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Stream<Action> generateActions(ImmutablePlanState state, List<Action> plannedActions,
            ArrayTable<String, String, Integer> distanceMatrix, Set<Package> packagesUnfinished) {
        if (hasCycle(plannedActions)) { // TODO: Convert to non-generation
            return Stream.empty();
        }

        Stream.Builder<Action> generated = Stream.builder();
        List<Vehicle> vehicles = new ArrayList<>(state.getAllVehicles());

        Domain domain = state.getDomain();
        RoadGraph graph = state.getRoadGraph();

        Map<Location, List<Vehicle>> vehicleMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            Location current = vehicle.getLocation();
            vehicleMap.computeIfAbsent(current, c -> {
                List<Vehicle> list = new ArrayList<>();
                list.add(vehicle);
                return list;
            });
        }
        Map<Location, List<Package>> packageMap = new HashMap<>();
        for (Package pkg : packagesUnfinished) {
            Location current = pkg.getLocation();
            if (current != null) {
                packageMap.computeIfAbsent(current, c -> {
                    List<Package> list = new ArrayList<>();
                    list.add(pkg);
                    return list;
                });
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
                            generated.accept(domain.buildDrop(vehicle, target, pkg));
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

            List<Package> packages = packageMap.get(location);
            if (packages != null) {
                for (Package pkg : packages) {
                    if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                        generated.accept(domain.buildPickUp(vehicle, location, pkg));
                    }
                }
            }
        } else {
            Map<String, Set<String>> vehicleDroppedAfterLastMove = getPackagesDroppedAfterLastMoveMap(
                    state.getVehicleMap().size(), plannedActions);
            packageMap.keySet().forEach(location -> {
                List<Package> packages = packageMap.get(location);
                List<Vehicle> vehiclesAtLoc = vehicleMap.get(location);
                if (packages == null || vehiclesAtLoc == null) {
                    return;
                }

                for (Vehicle vehicle : vehiclesAtLoc) {
                    Set<String> droppedNames = vehicleDroppedAfterLastMove.get(vehicle.getName());
                    for (Package pkg : packages) {
                        if (droppedNames != null && droppedNames.contains(pkg.getName())) {
                            continue;
                        }
                        if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                            generated.accept(domain.buildPickUp(vehicle, location, pkg));
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
                    if (pickupWhereDropoff(plannedActions, drop)) {
                        continue;
                    }
                    generated.accept(drop);
                }
            } else {
                for (Vehicle vehicle : vehicles) {
                    Location current = vehicle.getLocation();
                    for (Package pkg : vehicle.getPackageList()) {
                        Drop drop = domain.buildDrop(vehicle, current, pkg);
                        if (pickupWhereDropoff(plannedActions, drop)) {
                            continue;
                        }
                        generated.accept(drop);
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
            generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, plannedActions).forEach(generated::add);
        } else {
            for (Vehicle vehicle : vehicles) {
                generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, plannedActions)
                        .forEach(generated::add);
            }
        }

        return generated.build();
    }

    // Vehicle -> [Package]
    private static Map<String, Set<String>> getPackagesDroppedAfterLastMoveMap(int vehicleCount,
            List<Action> plannedActions) {
        // Vehicle -> int (index into plannedActions)
        Map<String, Integer> lastDriveIndexMap = new HashMap<>(vehicleCount);
        for (int i = 0; i < plannedActions.size(); i++) {
            Action action = plannedActions.get(i);
            Drive drive;
            if (!(action instanceof Drive)) {
                continue;
            }
            drive = (Drive) action;
            lastDriveIndexMap.put(drive.getWho().getName(), i);
        }

        Map<String, Set<String>> packagesDroppedAfterLastMoveMap = new HashMap<>(lastDriveIndexMap.size());
        for (Map.Entry<String, Integer> entry : lastDriveIndexMap.entrySet()) {
            String vehicleName = entry.getKey();
            int lastDriveIndex = entry.getValue();

            for (int i = lastDriveIndex + 1; i < plannedActions.size(); i++) {
                Action action = plannedActions.get(i);
                if (action instanceof Drop && vehicleName.equals(action.getWho().getName())) {
                    packagesDroppedAfterLastMoveMap.computeIfAbsent(vehicleName, v -> new HashSet<>())
                            .add(action.getWhat().getName());
                }
            }
        }
        return packagesDroppedAfterLastMoveMap;
    }

    private static Stream<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain,
            ArrayTable<String, String, Integer> distanceMatrix, List<Action> plannedActions) {
        Stream.Builder<Drive> vehicleActions = Stream.builder();
        Location current = vehicle.getLocation();
        for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
            Location target = graph.getLocation(edge.getTargetNode().getId());
            if (doesShorterPathExist(vehicle, target, plannedActions, distanceMatrix)) {
                continue;
            }
            vehicleActions.accept(domain.buildDrive(vehicle, current, target, graph.getRoad(edge.getId())));
        }
        return vehicleActions.build();
    }

    static boolean doesShorterPathExist(Vehicle vehicle, Location target, List<Action> plannedActions,
            // TODO: this is the bottleneck now + GC (List$Cons)
            ArrayTable<String, String, Integer> distanceMatrix) {
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

        return distanceMatrix.get(sourceOfPreviousDrives.getName(), target.getName()) <= lengthOfPath;
    }

    static int calculateSumOfDistancesToPackageTargets(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, Integer> distanceMatrix) {
        int sumDistances = 0;
        for (Vehicle vehicle : vehicleList) { // vehicles are never in the middle of a drive
            for (Package pkg : vehicle.getPackageList()) {
                sumDistances += distanceMatrix.get(vehicle.getLocation().getName(), pkg.getTarget().getName())
                        + 1; // + drop action
            }
        }
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                sumDistances += distanceMatrix.get(pkgLocation.getName(), pkg.getTarget().getName())
                        + 2; // + pickup and drop
            }
        }
        return sumDistances;
    }

    public static double getLengthToCorrect(APSP.APSPInfo current, String targetName) {
        if (current.getNodeId().equals(targetName)) { // fix weird behavior of APSP
            return 0d;
        } else {
            return current.getLengthTo(targetName);
        }
    }

    @Override
    public ForwardAstarPlanner copy() {
        return new ForwardAstarPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForwardAstarPlanner;
    }
}
