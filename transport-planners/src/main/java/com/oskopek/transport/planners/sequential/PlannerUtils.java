package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
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
        List<Action> plannedActions = state.getActions();
        if (PlannerUtils.hasCycle(plannedActions)) { // TODO: Convert to non-generation
            return Stream.empty();
        }

        Stream.Builder<Action> generated = Stream.builder();
        Collection<Vehicle> vehicles = state.getAllVehicles();
        RoadGraph graph = state.getRoadGraph();
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

        Optional<Action> lastAction = plannedActions.isEmpty() ? Optional.empty()
                : Optional.of(plannedActions.get(plannedActions.size() - 1));
        // pick-up
        Optional<Vehicle> lastVehicleAndLastDrive = lastAction
                .filter(a -> a instanceof Drive).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getVehicle(v.getName()));
        if (lastVehicleAndLastDrive.isPresent()) { // only use active vehicle
            Vehicle vehicle = lastVehicleAndLastDrive.get();
            Location location = vehicle.getLocation();

            Set<Package> packages = packageMap.get(location);
            if (packages != null) {
                for (Package pkg : packages) {
                    if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                        generated.accept(domain.buildPickUp(vehicle, location, pkg));
                    }
                }
            }
        } else {
            Map<String, Set<String>> vehicleDroppedAfterLastMove = PlannerUtils
                    .getPackagesDroppedAfterLastMoveMap(state.getVehicleMap().size(), plannedActions);
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
                    if (PlannerUtils.pickupWhereDropoff(plannedActions, drop)) {
                        continue;
                    }
                    generated.accept(drop);
                }
            } else {
                for (Vehicle vehicle : vehicles) {
                    Location current = vehicle.getLocation();
                    for (Package pkg : vehicle.getPackageList()) {
                        Drop drop = domain.buildDrop(vehicle, current, pkg);
                        if (PlannerUtils.pickupWhereDropoff(plannedActions, drop)) {
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
            PlannerUtils.generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, plannedActions)
                    .forEach(generated::add);
        } else {
            for (Vehicle vehicle : vehicles) {
                PlannerUtils.generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, plannedActions)
                        .forEach(generated::add);
            }
        }
        return generated.build();
    }

    public static Stream<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain,
            ArrayTable<String, String, Integer> distanceMatrix, List<Action> plannedActions) {
        Stream.Builder<Drive> vehicleActions = Stream.builder();
        Location current = vehicle.getLocation();
        for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
            Location target = graph.getLocation(edge.getTargetNode().getId());
            if (PlannerUtils.doesShorterPathExist(vehicle, target, plannedActions, distanceMatrix)) {
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


    public static boolean doesShorterPathExist(Vehicle vehicle, Location target, List<Action> plannedActions,
            ArrayTable<String, String, Integer> distanceMatrix) {
        // TODO: this is the bottleneck now + GC (List$Cons)
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

    public static boolean hasCycle(List<Action> plannedActions) {
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

    public static boolean pickupWhereDropoff(List<Action> plannedActions, Action newAction) {
        Map<String, Set<String>> pickedUpAt = new HashMap<>(plannedActions.size() + 1);
        for (Action a : plannedActions) {
            if (a instanceof PickUp) {
                pickedUpAt.computeIfAbsent(a.getWhat().getName(), s -> new HashSet<>(2))
                        .add(a.getWhere().getName());
            }
        }
        if (newAction instanceof PickUp) {
            pickedUpAt.computeIfAbsent(newAction.getWhat().getName(), s -> new HashSet<>(2))
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

}
