package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.state.ImmutablePlanState;
import javaslang.collection.*;
import javaslang.collection.List;
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.graphstream.graph.Edge;
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

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
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

            generateActions(state, actions).map(state::apply).filter(Optional::isPresent).map(Optional::get)
                    .forEach(states::addLast);

            counter++;
            if (counter % 100_000 == 0) {
                logger.debug("Explored {} states, left: {}", counter, states.size());
                if (counter % 1_000_000 == 0) {
                    logger.debug("GC");
                    System.gc();
                }
            }
        }

        return Optional.empty();
    }

    private static List<Action> generateActions(ImmutablePlanState state, List<Action> plannedActions) {
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
                            return List.ofAll(generated);
                        }
                    }
                }
            }
        }

        // pick-up
        Optional<Vehicle> lastVehicleAndLastDrive = plannedActions.lastOption()
                .filter(a -> a instanceof Drive).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getVehicle(v.getName())).toJavaOptional();
        if (lastVehicleAndLastDrive.isPresent()) { // only use active vehicle
            Vehicle vehicle = lastVehicleAndLastDrive.get();
            Location location = vehicle.getLocation();

            // START detect cycles
            List<String> drives = Stream.ofAll(plannedActions).reverse().takeWhile(a -> a instanceof Drive)
                    .map(d -> d.getWhere().getName()).toList();
            drives = drives.append(plannedActions.last().getWhat().getName()); // add last target
            java.util.Set<String> drivesSet = new HashSet<>();
            for (String loc : drives) {
                if (!drivesSet.add(loc)) {
                    // CYCLE!
                    return List.empty();
                }
            }
            // END detect cycles


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
            packageMap.keySet().forEach(location -> {
                List<Package> packages = packageMap.get(location);
                List<Vehicle> vehicles = vehicleMap.get(location);
                if (packages == null || vehicles == null) {
                    return;
                }

                for (Package pkg : packages) {
                    for (Vehicle vehicle : vehicles) {
                        generated.add(domain.buildPickUp(vehicle, location, pkg));
                    }
                }
            });
        }

        // drop

        if (plannedActions.lastOption().filter(a -> !(a instanceof PickUp)).isDefined()) { // do not drop after pick up
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
        if (lastVehicleAndLastDrive.isPresent()) { // continue driving if driving
            Vehicle vehicle = lastVehicleAndLastDrive.get();
            Location current = vehicle.getLocation();
            for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
                Location target = graph.getLocation(edge.getTargetNode().getId());
                generated.add(domain.buildDrive(vehicle, current, target, graph));
            }
        } else {
            for (Vehicle vehicle : state.getAllVehicles()) {
                Location current = vehicle.getLocation();
                for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
                    Location target = graph.getLocation(edge.getTargetNode().getId());
                    generated.add(domain.buildDrive(vehicle, current, target, graph));
                }
            }
        }

        return List.ofAll(generated);
    }

    @Override
    public String getName() {
        return SequentialForwardBFSPlanner.class.getName();
    }

    @Override
    public SequentialForwardBFSPlanner copy() {
        return new SequentialForwardBFSPlanner();
    }
}
