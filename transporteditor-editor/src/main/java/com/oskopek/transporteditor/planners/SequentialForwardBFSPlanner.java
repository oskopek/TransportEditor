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
import javaslang.collection.*;
import javaslang.collection.List;
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.*;

public class SequentialForwardBFSPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SequentialForwardBFSPlanner() {
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

        Deque<ImmutablePlanState> states = new ArrayDeque<>();
        states.add(new ImmutablePlanState(domain, problem, List.empty()));

        logger.debug("Starting planning...");

        long counter = 0;
        List<Action> actions = List.empty();
        while (!states.isEmpty()) {
            if (shouldCancel()) {
                return Optional.empty();
            }
            ImmutablePlanState state = states.removeFirst();
            if (state.getActions().size() > actions.size()) {
                logger.debug("Enlarged plan: {} actions", state.getActions().size());
                logger.debug("Explored {} states, left: {}", counter, states.size());
            }
            actions = state.getActions();

            if (state.isGoalState()) {
                logger.debug("Found goal state! Exiting. Explored {} states. Left out {} states.", counter, states.size());
                return Optional.of(new SequentialPlan(actions.toJavaList()));
            }

            generateActions(state, actions, originalAPSPGraph).forEach(action -> state.apply(action).ifPresent(states::addLast));

            counter++;
            if (counter % 100_000 == 0) {
                logger.debug("Explored {} states, left: {}", counter, states.size());
//                if (counter % 1_000_000 == 0) {
//                    logger.debug("GC");
//                    System.gc();
//                }
            }
        }

        return Optional.empty();
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
        java.util.Set<String> drives = new HashSet<>();
        int index;
        for (index = plannedActions.size() - 1; index >= 0; index--) {
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
        if (index + 1 <= plannedActions.size()) {
            if (!drives.add(plannedActions.get(index + 1).getWhat().getName())) { // add last target
                // CYCLE!
                return true;
            }
        }
        return false;
    }

    private static java.util.List<Action> generateActions(ImmutablePlanState state, List<Action> plannedActions, RoadGraph originalAPSPGraph) {
        java.util.List<Action> generated = new ArrayList<>();
        List<Package> packagesUnfinished = Stream.ofAll(state.getAllPackages()).filter(p -> !p.getTarget().equals(p.getLocation())).toList();

        Domain domain = state.getDomain();
        RoadGraph graph = state.getRoadGraph();

        Map<Location, List<Vehicle>> vehicleMap = new HashMap<>();
        for (Vehicle vehicle : state.getAllVehicles()) {
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
                List<Vehicle> vehicles = vehicleMap.get(target);
                if (vehicles != null) {
                    for (Vehicle vehicle : vehicles) { // vehicles at target
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

            if (hasCycle(plannedActions)) {
                return Collections.emptyList();
            }

            if (!location.equals(plannedActions.last().getWhat())) {
                throw new IllegalStateException("Planner assumptions broken.");
            }
            List<Package> packages = packageMap.get(location);
            if (packages != null) {
                for (Package pkg : packages) {
                    generated.add(domain.buildPickUp(vehicle, location, pkg));
                }
            }
        } else {
            java.util.Set<Tuple2<String, String>> dropped = didDropThisJustNow(plannedActions);
            packageMap.keySet().forEach(location -> {
                List<Package> packages = packageMap.get(location);
                List<Vehicle> vehicles = vehicleMap.get(location);
                if (packages == null || vehicles == null) {
                    return;
                }

                for (Package pkg : packages) {
                    for (Vehicle vehicle : vehicles) {
                        if (dropped.contains(Tuple.of(vehicle.getName(), pkg.getName()))) {
                            continue;
                        }
                        generated.add(domain.buildPickUp(vehicle, location, pkg));
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
                    generated.add(domain.buildDrop(vehicle, current, pkg));
                }
            } else {
                for (Vehicle vehicle : state.getAllVehicles()) {
                    Location current = vehicle.getLocation();
                    for (Package pkg : vehicle.getPackageList()) {
                        generated.add(domain.buildDrop(vehicle, current, pkg));
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
            generated.addAll(generateDrivesForVehicle(vehicle, graph, domain, originalAPSPGraph));
        } else {
            for (Vehicle vehicle : state.getAllVehicles()) {
                generated.addAll(generateDrivesForVehicle(vehicle, graph, domain, originalAPSPGraph));
            }
        }

        return generated;
    }

    private static java.util.List<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain, RoadGraph originalAPSPGraph) {
        java.util.List<Drive> vehicleActions = new ArrayList<>();
        Location current = vehicle.getLocation();
        for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
            Location target = graph.getLocation(edge.getTargetNode().getId());
            vehicleActions.add(domain.buildDrive(vehicle, current, target, graph.getRoad(edge.getId())));
        }
        if (!vehicle.getPackageList().isEmpty()) {
            Map<Location, Integer> sumOfDistancesToPackageTargets = calculateSumOfDistancesToPackageTargets(vehicle,
                    originalAPSPGraph);
            vehicleActions.sort(Comparator.comparing(d -> sumOfDistancesToPackageTargets.get(d.getWhat())));
        }
        return vehicleActions;
    }

    private static Map<Location, Integer> calculateSumOfDistancesToPackageTargets(Vehicle vehicle, RoadGraph graph) {
        Map<Location, Integer> map = new HashMap<>(graph.getNodeCount());
        graph.getAllLocations().forEach(location -> {
            double sum = 0;
            APSP.APSPInfo info = graph.getNode(location.getName()).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (Package pkg : vehicle.getPackageList()) {
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

    @Override
    public String getName() {
        return SequentialForwardBFSPlanner.class.getName();
    }

    @Override
    public SequentialForwardBFSPlanner copy() {
        return new SequentialForwardBFSPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SequentialForwardBFSPlanner;
    }
}
