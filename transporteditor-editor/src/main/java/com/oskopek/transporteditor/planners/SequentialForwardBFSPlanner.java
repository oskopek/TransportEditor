package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.state.ImmutablePlanState;
import javaslang.collection.*;
import javaslang.collection.List;
import org.graphstream.graph.Edge;

import java.util.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.*;

public class SequentialForwardBFSPlanner extends AbstractPlanner {

    public SequentialForwardBFSPlanner() {
        // intentionally empty
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        Deque<ImmutablePlanState> states = new ArrayDeque<>();
        states.add(new ImmutablePlanState(domain, problem, List.empty()));

        while (!states.isEmpty()) {
            ImmutablePlanState state = states.removeFirst();
            List<Action> actions = state.getActions();
            if (state.isGoalState()) {
                return Optional.of(new SequentialPlan(actions.toJavaList()));
            }

            generateActions(state, actions).map(state::apply).filter(Optional::isPresent).map(Optional::get)
                    .forEach(s -> states.addLast(s));
        }

        return Optional.empty();
    }

    private static List<Action> generateActions(ImmutablePlanState state, List<Action> actions) {
        java.util.List<Action> generated = new ArrayList<>();

        Domain domain = state.getDomain();
        RoadGraph graph = state.getRoadGraph();

        Map<Location, List<Vehicle>> vehicleMap = new HashMap<>();
        for (Vehicle vehicle : state.getAllVehicles()) {
            Location current = vehicle.getLocation();
            vehicleMap.put(current, vehicleMap.getOrDefault(current, List.empty()).append(vehicle));
        }
        Map<Location, List<Package>> packageMap = new HashMap<>();
        for (Package pkg : state.getAllPackages()) {
            Location current = pkg.getLocation();
            if (current != null) {
                packageMap.put(current, packageMap.getOrDefault(current, List.empty()).append(pkg));
            }
        }

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

        // drop
        for (Vehicle vehicle : state.getAllVehicles()) {
            Location current = vehicle.getLocation();
            for (Package pkg : vehicle.getPackageList()) {
                generated.add(domain.buildDrop(vehicle, current, pkg));
            }
        }

        // drive
        for (Vehicle vehicle : state.getAllVehicles()) {
            Location current = vehicle.getLocation();
            for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
                Location target = graph.getLocation(edge.getTargetNode().getId());
                generated.add(domain.buildDrive(vehicle, current, target, graph));
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
