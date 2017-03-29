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
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.*;

public class SequentialForwardBFSPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SequentialForwardBFSPlanner() {
        setName(SequentialForwardAstarPlanner.class.getSimpleName());
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
        states.add(new ImmutablePlanState(domain, problem, Collections.emptyList()));

        logger.debug("Starting planning...");

        long counter = 0;
        List<Action> actions = Collections.emptyList();
        while (!states.isEmpty()) {
            if (shouldCancel()) {
                logger.debug("Returning current hypothesis plan after cancellation.");
                return Optional.of(new SequentialPlan(actions));
            }
            ImmutablePlanState state = states.removeFirst();
            if (state.getActions().size() > actions.size()) {
                logger.debug("Enlarged plan: {} actions", state.getActions().size());
                logger.debug("Explored {} states, left: {}", counter, states.size());
            }
            actions = state.getActions();
//            verifyActionsOfOptimalP02Plan(actions);

            if (state.isGoalState()) {
                logger.debug("Found goal state! Exiting. Explored {} states. Left out {} states.", counter, states.size());
                return Optional.of(new SequentialPlan(actions));
            }

            java.util.List<Action> generatedActions = generateActions(state, actions, originalAPSPGraph);

            java.util.List<ImmutablePlanState> generatedStates = new ArrayList<>(generatedActions.size());
            int minTotalTime = Integer.MAX_VALUE;
            for (Action generatedAction : generatedActions) {
                Optional<ImmutablePlanState> maybeNewState = state.apply(generatedAction);
                if (maybeNewState.isPresent()) {
                    ImmutablePlanState newState = maybeNewState.get();
                    int totalTime = newState.getTotalTime();
                    if (totalTime < minTotalTime) {
                        minTotalTime = totalTime;
                    }
                    generatedStates.add(newState);
                }
            }
            generatedStates.stream().sorted(Comparator.comparing(ImmutablePlanState::getTotalTime)).forEach(states::addLast);

            counter++;
            if (counter % 100_000 == 0) {
                logger.debug("Explored {} states, left: {}", counter, states.size());
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

    private static java.util.List<Action> generateActions(ImmutablePlanState state, List<Action> plannedActions, RoadGraph originalAPSPGraph) {
        java.util.List<Action> generated = new ArrayList<>();
        java.util.List<Package> packagesUnfinished = state.getAllPackages().stream().filter(p -> !p.getTarget().equals(p.getLocation())).collect(Collectors.toList());
//        java.util.List<Vehicle> vehicles = Collections.singletonList(state.getVehicle("truck-2"));
        java.util.List<Vehicle> vehicles = new ArrayList<>(state.getAllVehicles());


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

            if (SequentialForwardAstarPlanner.hasCycle(plannedActions)) {
                return Collections.emptyList();
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
                    generated.add(domain.buildDrop(vehicle, current, pkg));
                }
            } else {
                for (Vehicle vehicle : vehicles) {
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
            for (Vehicle vehicle : vehicles) {
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
                distance = SequentialForwardAstarPlanner.getLengthToCorrect(info, target.getName());
                sum += distance;
            }
            map.put(location, (int) sum);
        });
        return map;
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
