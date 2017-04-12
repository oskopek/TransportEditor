package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.domain.action.Drop;
import com.oskopek.transport.model.domain.action.PickUp;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.implementations.Graphs;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Various utility methods for calculating heuristics and other.
 */
public final class PlannerUtils {

    /**
     * Default empty constructor.
     */
    private PlannerUtils() {
        // intentionally empty
    }

    public static Stream<Action> generateActions(Domain domain, ImmutablePlanState state,
            ArrayTable<String, String, Integer> distanceMatrix, Set<Package> packagesUnfinished) {
        if (PlannerUtils.hasCycle(state.getAllActionsReversed())) { // TODO: Convert to non-generation
            return Stream.empty();
        }

        Stream.Builder<Action> generated = Stream.builder();
        Collection<Vehicle> vehicles = state.getProblem().getAllVehicles();
        RoadGraph graph = state.getProblem().getRoadGraph();
        Map<Location, Set<Vehicle>> vehicleMap = PlannerUtils.computeVehicleMap(vehicles);
        Map<Location, Set<Package>> packageMap = PlannerUtils.computePackageMap(packagesUnfinished);

        // drop at target above all else
        for (Package pkg : packagesUnfinished) {
            if (pkg.getLocation() == null) { // unfinished package is in vehicle
                Location target = pkg.getTarget();
                Set<Vehicle> vehiclesAtLoc = vehicleMap.get(target);
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

        Optional<Action> lastAction = Optional.ofNullable(state.getAction());
        // pick-up
        Optional<Vehicle> lastVehicleAndLastDrive = lastAction
                .filter(a -> a instanceof Drive).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getProblem().getVehicle(v.getName()));
        if (lastVehicleAndLastDrive.isPresent()) { // only use active vehicle
            Vehicle vehicle = lastVehicleAndLastDrive.get();
            Location location = vehicle.getLocation();

            Set<Package> packages = packageMap.get(location);
            if (packages != null) {
                for (Package pkg : packages) {
                    if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                        PickUp nextAction = domain.buildPickUp(vehicle, location, pkg);
                        if (PlannerUtils.needlessDropAndPickupOccurred(state.getProblem().getAllVehicles(),
                                state.getAllActionsInList(), nextAction)) {
                            continue;
                        }
                        generated.accept(nextAction);
                    }
                }
            }
        } else {
            Map<String, Set<String>> vehicleDroppedAfterLastMove = PlannerUtils
                    .getPackagesDroppedAfterLastMoveMap(vehicles.size(), state.getAllActionsInList());
            packageMap.keySet().forEach(location -> {
                Set<Package> packages = packageMap.get(location);
                Set<Vehicle> vehiclesAtLoc = vehicleMap.get(location);
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
                            PickUp nextAction = domain.buildPickUp(vehicle, location, pkg);
                            if (PlannerUtils.needlessDropAndPickupOccurred(state.getProblem().getAllVehicles(),
                                    state.getAllActionsInList(), nextAction)) {
                                continue;
                            }
                            generated.accept(nextAction);
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
                    if (PlannerUtils.droppedPackageWhereWePickedItUp(state, drop)) {
                        continue;
                    }
                    generated.accept(drop);
                }
            } else {
                for (Vehicle vehicle : vehicles) {
                    Location current = vehicle.getLocation();
                    for (Package pkg : vehicle.getPackageList()) {
                        Drop drop = domain.buildDrop(vehicle, current, pkg);
                        if (PlannerUtils.droppedPackageWhereWePickedItUp(state, drop)) {
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
                .map(v -> state.getProblem().getVehicle(v.getName()));

        List<Action> reversedActions = Lists.newArrayList(state.getAllActionsReversed());
        if (lastVehicleAndNotDrop.isPresent()) { // continue driving if driving
            Vehicle vehicle = lastVehicleAndNotDrop.get();
            PlannerUtils.generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, reversedActions)
                    .forEach(generated::add);
        } else {
            for (Vehicle vehicle : vehicles) {
                PlannerUtils.generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, reversedActions)
                        .forEach(generated::add);
            }
        }
        return generated.build();
    }

    @Deprecated // doesn't work, use ...Occurred
    public static boolean needlessDropAndPickup(Iterator<Action> reverseActions, Vehicle vehicle, Package pkg) {
        Map<String, String> pickedUpBy = new HashMap<>(); // Package -> Vehicle
        pickedUpBy.put(pkg.getName(), vehicle.getName());

        while (reverseActions.hasNext()) {
            Action action = reverseActions.next();
            if (action instanceof PickUp) {
                pickedUpBy.put(action.getWhat().getName(), action.getWho().getName());
            } else if (action instanceof Drop) {
                String packageName = action.getWhat().getName();
                if (action.getWho().getName().equals(pickedUpBy.get(packageName))) {
                    return true;
                } else {
                    pickedUpBy.remove(packageName);
                }
            }
        }
        return false;
    }

    @Deprecated // Slow, replace
    public static boolean needlessDropAndPickupOccurred(Collection<Vehicle> vehicles, Iterable<Action> actions,
            PickUp lastAction) {
        List<Action> actionsNew = Lists.newArrayList(actions);
        actionsNew.add(lastAction);
        return needlessDropAndPickupOccurred(vehicles, actionsNew);
    }

    @Deprecated // Slow, replace
    public static boolean needlessDropAndPickupOccurred(Collection<Vehicle> vehicles, Iterable<Action> actions) {
        for (Vehicle v : vehicles) {
            Map<String, Integer> packagesUntouchedSince = new HashMap<>();
            List<Integer> capacities = new ArrayList<>();
            int index = 0;
            int lastCapacity = v.getMaxCapacity().getCost();
            for (Action action : actions) {
                if (action.getWho().getName().equals(v.getName())) {
                    if (action instanceof Drop) {
                        capacities.add(++lastCapacity);
                        packagesUntouchedSince.put(action.getWhat().getName(), index);
                    } else if (action instanceof PickUp) {
                        capacities.add(--lastCapacity);
                        if (packagesUntouchedSince.containsKey(action.getWhat().getName())) {
                            if (!capacities.subList(packagesUntouchedSince.get(action.getWhat().getName()), index)
                                    .contains(0)) {
//                                System.out.println(action);
                                return true;
                            }
                        }
                    } else {
                        capacities.add(lastCapacity);
                    }
                } else {
                    capacities.add(lastCapacity);
                    packagesUntouchedSince.remove(action.getWhat().getName());
                }
                index++;
            }
        }
        return false;
    }

    public static Stream<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain,
            ArrayTable<String, String, Integer> distanceMatrix, Iterable<Action> reversedActions) {
        Stream.Builder<Drive> vehicleActions = Stream.builder();
        Location current = vehicle.getLocation();
        for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
            Location target = graph.getLocation(edge.getTargetNode().getId());
            if (PlannerUtils.doesShorterPathExist(vehicle, target, reversedActions.iterator(), distanceMatrix)) {
                continue;
            }
            vehicleActions.accept(domain.buildDrive(vehicle, current, target, graph.getRoad(edge.getId())));
        }
        return vehicleActions.build();
    }

    // Vehicle -> [Package]
    public static Map<String, Set<String>> getPackagesDroppedAfterLastMoveMap(int vehicleCount,
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
                    packagesDroppedAfterLastMoveMap.computeIfAbsent(vehicleName, v -> new HashSet<>(2))
                            .add(action.getWhat().getName());
                }
            }
        }
        return packagesDroppedAfterLastMoveMap;
    }

    public static Map<Location, Set<Vehicle>> computeVehicleMap(Collection<Vehicle> vehicles) {
        Map<Location, Set<Vehicle>> vehicleMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            Location current = vehicle.getLocation();
            vehicleMap.computeIfAbsent(current, c -> {
                Set<Vehicle> set = new HashSet<>();
                set.add(vehicle);
                return set;
            });
        }
        return vehicleMap;
    }

    public static Map<Location, Set<Package>> computePackageMap(Collection<Package> pkgs) {
        Map<Location, Set<Package>> pkgMap = new HashMap<>();
        for (Package pkg : pkgs) {
            Location current = pkg.getLocation();
            pkgMap.computeIfAbsent(current, c -> {
                Set<Package> set = new HashSet<>(2);
                set.add(pkg);
                return set;
            });
        }
        return pkgMap;
    }

    public static ArrayTable<String, String, Integer> computeAPSP(final RoadGraph graph) {
        final String ATTRIBUTE_NAME = "weight";
        RoadGraph originalAPSPGraph = (RoadGraph) Graphs.clone(graph);
        originalAPSPGraph.getAllRoads().forEach(roadEdge -> originalAPSPGraph.getEdge(roadEdge.getRoad().getName())
                .addAttribute(ATTRIBUTE_NAME, roadEdge.getRoad().getLength().getCost()));
        new APSP(originalAPSPGraph, ATTRIBUTE_NAME, true).compute();
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
        return distanceMatrix;
    }

    public static double getLengthToCorrect(APSP.APSPInfo current, String targetName) {
        if (targetName.equals(current.getNodeId())) { // fix weird behavior of APSP
            return 0d;
        } else {
            return current.getLengthTo(targetName);
        }
    }


    public static boolean doesShorterPathExist(Vehicle vehicle, Location target,
            Iterator<Action> reversedActionsIterator, ArrayTable<String, String, Integer> distanceMatrix) {

        if (!reversedActionsIterator.hasNext()) {
            return false;
        }

        Drive lastDrive = null;
        int lengthOfPath = 0;
        Location sourceOfPreviousDrives;


        while (reversedActionsIterator.hasNext()) {
            Action plannedAction = reversedActionsIterator.next();
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

        return distanceMatrix.get(sourceOfPreviousDrives.getName(), target.getName()) < lengthOfPath;
    }

    public static int calculateSumOfDistancesToPackageTargets(Collection<Package> packageList,
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

    public static int admissibleHeuristic(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, Integer> distanceMatrix) {
        int sumDistances = 0;
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                sumDistances += 2;
            } else {
                sumDistances += 1;
            }
        }
        return sumDistances;
    }

    public static int calculateSumOfDistancesToVehiclesPackageTargetsAdmissible(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, Integer> distanceMatrix) {
        int sumDistances = 0;
//        for (Vehicle vehicle : vehicleList) { // vehicles are never in the middle of a drive
//            int maxPkgDistance = 0;
//            for (Package pkg : vehicle.getPackageList()) {
//                int dist = distanceMatrix.get(vehicle.getLocation().getName(), pkg.getTarget().getName());
//                if (dist > maxPkgDistance) {
//                    maxPkgDistance = dist;
//                }
//            } // TODO: not true, calculate the max distance for a package in the vehicle, or the spanning tree distances
//            sumDistances += maxPkgDistance + vehicle.getPackageList().size(); // + drop actions
//        }
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                String pkgLocName = pkgLocation.getName();
                // calculate the distance to the target + pickup and drop
                sumDistances += distanceMatrix.get(pkgLocName, pkg.getTarget().getName()) // TODO: not admissible
                        + 2; // + pickup and drop

                // Calculate the distance to the nearest vehicle or package
                int minVehicleDistance = Integer.MAX_VALUE;
                for (Vehicle vehicle : vehicleList) {
                    int dist = distanceMatrix.get(pkgLocName, vehicle.getLocation().getName());
                    if (dist < minVehicleDistance) {
                        minVehicleDistance = dist;
                    }
                }
                for (Package pkg2 : packageList) {
                    if (pkg2.getLocation() != null) {
                        int dist = distanceMatrix.get(pkgLocName, pkg2.getLocation().getName());
                        if (dist < minVehicleDistance) {
                            minVehicleDistance = dist;
                        }
                    }
                }
                sumDistances += minVehicleDistance;
            } else {
                sumDistances += 1; // drop, at least
            }
        }
        return sumDistances;
    }

    public static int calculateSumOfDistancesToVehiclesPackageTargets(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, Integer> distanceMatrix) {
        int sumDistances = 0;
        for (Vehicle vehicle : vehicleList) { // vehicles are never in the middle of a drive
            int maxPkgDistance = 0;
            for (Package pkg : vehicle.getPackageList()) {
                int dist = distanceMatrix.get(vehicle.getLocation().getName(), pkg.getTarget().getName());
                if (dist > maxPkgDistance) {
                    maxPkgDistance = dist;
                }
            } // TODO: not true, calculate the max distance for a package in the vehicle, or the spanning tree distances
            sumDistances += maxPkgDistance + vehicle.getPackageList().size(); // + drop actions
        }
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                String pkgLocName = pkgLocation.getName();
                // calculate the distance to the target + pickup and drop
                sumDistances += distanceMatrix.get(pkgLocName, pkg.getTarget().getName())
                        + 2; // + pickup and drop

                // Calculate the distance to the nearest vehicle
                int minVehicleDistance = Integer.MAX_VALUE;
                for (Vehicle vehicle : vehicleList) {
                    int dist = distanceMatrix.get(pkgLocName, vehicle.getLocation().getName());
                    if (dist < minVehicleDistance) {
                        minVehicleDistance = dist;
                    }
                }
                sumDistances += minVehicleDistance;
            }
        }
        return sumDistances;
    }

    public static boolean hasCycle(Iterator<Action> reversedActions) {
        Set<String> drives = new HashSet<>();
        if (!reversedActions.hasNext()) {
            return false;
        }
        Action lastAction = reversedActions.next();
        if (!reversedActions.hasNext()) { // has to have at least two actions
            return false;
        }
        if (lastAction instanceof Drive) { // add last target
            drives.add(lastAction.getWhat().getName());
        }
        while (reversedActions.hasNext()) {
            Action plannedAction = reversedActions.next();
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

    public static Set<Package> getUnfinishedPackages(Collection<Package> packages) {
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

    public static boolean droppedPackageWhereWePickedItUp(ImmutablePlanState state, Drop newAction) {
        Map<String, Set<String>> pickedUpAt = new HashMap<>();
        for (Iterator<Action> it = state.getAllActionsReversed(); it.hasNext();) {
            Action a = it.next();
            if (a instanceof PickUp) {
                pickedUpAt.computeIfAbsent(a.getWhat().getName(), s -> new HashSet<>(2))
                        .add(a.getWhere().getName());
            }
        }


        Set<String> pickedBySameCar = pickedUpAt.get(newAction.getWhat().getName());
        if (pickedBySameCar != null && pickedBySameCar.contains(newAction.getWhere().getName())) {
            return true;
        }
        for (Iterator<Action> it = state.getAllActionsReversed(); it.hasNext();) {
            Action a = it.next();
            if (a instanceof Drop) {
                pickedBySameCar = pickedUpAt.get(newAction.getWhat().getName());
                if (pickedBySameCar != null && pickedBySameCar.contains(newAction.getWhere().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
