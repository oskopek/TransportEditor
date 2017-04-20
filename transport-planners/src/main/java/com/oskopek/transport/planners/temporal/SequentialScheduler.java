package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drop;
import com.oskopek.transport.model.domain.action.PickUp;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.planners.AbstractPlanner;
import javaslang.Tuple;
import javaslang.collection.Stream;
import org.graphstream.algorithm.TopologicalSort;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SequentialScheduler extends AbstractPlanner {

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        SequentialDomain seqDomain = new SequentialDomain(domain.getName() + "-seq");
        Problem seqProblem = translateToSequential(problem);
        Optional<Plan> sequentialPlan = planInternal(seqDomain, seqProblem, plan -> schedule(problem, plan.getActions()));
        return sequentialPlan.map(plan -> {
            if (plan instanceof TemporalPlan) {
                return plan;
            }
            return schedule(seqProblem, plan.getActions());
        });
    }

    protected abstract Optional<Plan> planInternal(Domain seqDomain, Problem seqProblem, Function<Plan, Plan> planTransformation);

    protected abstract Planner getInternalPlanner();

    // 1. find mutexes in plan.. ordered pairs of actions
    // 2. plan actions with no mutexes at 0 and incrementally plan others after the max mutex of previous ones (DAG)
    // Mutexes:
    // * Actions of the same vehicle
    // * drop/pick-up of the same package
    // TODO: handle fuel and refueling
    protected static TemporalPlan schedule(Problem temporalProblem, Collection<Action> seqActions) {
        if (seqActions.isEmpty()) {
            return new TemporalPlan(Collections.emptyList());
        }
        List<Action> seqActionList = new ArrayList<>(seqActions);
        Graph mutexDag = new MultiGraph("mutexDag", true, false, seqActionList.size(), seqActionList.size());
        for (int i = 0; i < seqActionList.size(); i++) {
            mutexDag.addNode(i + "");
        }
        int id = 0;
        for (int i = 0; i < seqActionList.size(); i++) {
            Action from = seqActionList.get(i);
            for (int j = i + 1; j < seqActionList.size(); j++) {
                Action to = seqActionList.get(j);
                if (from.getWho().getName().equals(to.getWho().getName())) { // vehicle mutex
                    mutexDag.addEdge(i + "->" + j + "_" + id++, i, j, true); // for sequential drive actions, only add the needed transitive ones
                    continue;
                }
                if (((from instanceof Drop && to instanceof PickUp) || (to instanceof Drop && from instanceof PickUp))
                        && from.getWhat().getName().equals(to.getWhat().getName())) {
                    mutexDag.addEdge(i + "->" + j + "_" + id++, i, j, true);
                    continue;
                }
            }
        }
        Map<Integer, TemporalPlanAction> plannedActions = new HashMap<>();
        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.KAHN);
        sort.init(mutexDag);
        sort.compute();
        List<Integer> topoSorted = sort.getSortedNodes().stream().map(n -> Integer.parseInt(n.getId())).collect(Collectors.toList());
        final double delta = 0.001;
        for (int actionIndex : topoSorted) {
            double maxEndTimeOfPrevious = 0d;
            for (Iterator<Edge> it = mutexDag.getNode(actionIndex).getEnteringEdgeIterator(); it.hasNext(); ) {
                Edge enteringEdge = it.next();
                int sourceActionIndex = Integer.parseInt(enteringEdge.getSourceNode().getId());
                TemporalPlanAction plannedAction = plannedActions.get(sourceActionIndex);
                maxEndTimeOfPrevious = Math.max(plannedAction.getEndTimestamp(), maxEndTimeOfPrevious) + delta;
            }
            Action action = seqActionList.get(actionIndex);
            plannedActions.put(actionIndex, new TemporalPlanAction(action, maxEndTimeOfPrevious, maxEndTimeOfPrevious + action.getDuration().getCost()));
        }
        return new TemporalPlan(plannedActions.values());
    }

    // remove all fuel-related stuff from vehicles and graph roads
    // TODO: verify that all seq. planners use package size correctly
    // TODO: vehicle goals
    protected static Problem translateToSequential(final Problem problem) {
        RoadGraph graph = problem.getRoadGraph();
        RoadGraph newGraph = new DefaultRoadGraph(graph.getId());
        graph.getAllLocations().map(l -> l.updateHasPetrolStation(false)).forEach(newGraph::addLocation);
        Map<String, Location> newLocMap = Stream.ofAll(newGraph.getAllLocations().map(l -> Tuple.of(l.getName(), l))
                .collect(Collectors.toList())).toJavaMap(v -> v);

        Problem seqProblem = problem;
        Iterator<Vehicle> newVehicles = seqProblem.getAllVehicles().stream().map(v -> v.updateCurFuelCapacity(null).updateMaxFuelCapacity(null).updateLocation(newLocMap.get(v.getLocation().getName()))).iterator();
        while (newVehicles.hasNext()) {
            Vehicle vehicle = newVehicles.next();
            seqProblem = seqProblem.putVehicle(vehicle.getName(), vehicle);
        }

        Iterator<Package> newPackages = seqProblem.getAllPackages().stream().map(p -> p.updateLocation(newLocMap.get(p.getLocation().getName())).updateTarget(newLocMap.get(p.getTarget().getName()))).iterator();
        while (newPackages.hasNext()) {
            Package pkg = newPackages.next();
            seqProblem = seqProblem.putPackage(pkg.getName(), pkg);
        }

        graph.getAllRoads().forEach(re -> newGraph.addRoad(new DefaultRoad(re.getRoad().getName(), re.getRoad().getLength()),
                newLocMap.get(re.getFrom().getName()), newLocMap.get(re.getTo().getName())));
        return new DefaultProblem(seqProblem.getName(), newGraph, seqProblem.getVehicleMap(), seqProblem.getPackageMap());
    }

    @Override
    public boolean cancel() {
        Planner internal = getInternalPlanner();
        if (internal != null) {
            internal.cancel();
        }
        return super.cancel();
    }

    @Override
    public abstract SequentialScheduler copy();

    private static final class Mutex {
        private final Action left;
        private final Action right;

        public Mutex(Action left, Action right) {
            this.left = left;
            this.right = right;
        }

        public Action getLeft() {
            return left;
        }

        public Action getRight() {
            return right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Mutex)) {
                return false;
            }
            Mutex mutex = (Mutex) o;
            if (getLeft() != null ? !getLeft().equals(mutex.getLeft()) : mutex.getLeft() != null) {
                return false;
            }
            return getRight() != null ? getRight().equals(mutex.getRight()) : mutex.getRight() == null;
        }

        @Override
        public int hashCode() {
            int result = getLeft() != null ? getLeft().hashCode() : 0;
            result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
            return result;
        }
    }
}
