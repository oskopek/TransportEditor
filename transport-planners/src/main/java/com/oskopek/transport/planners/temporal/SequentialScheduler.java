package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.planners.AbstractPlanner;
import javaslang.Tuple;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SequentialScheduler extends AbstractPlanner {

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        SequentialDomain seqDomain = new SequentialDomain(domain.getName() + "-seq");
        Problem seqProblem = translateToSequential(problem);
        Optional<Plan> sequentialPlan = planInternal(seqDomain, seqProblem, plan -> schedule(problem, plan.getTemporalPlanActions()));
        return sequentialPlan.map(plan -> schedule(seqProblem, plan.getTemporalPlanActions()));
    }

    protected abstract Optional<Plan> planInternal(Domain seqDomain, Problem seqProblem, Function<Plan, Plan> planTransformation);

    protected abstract Planner getInternalPlanner();

    // 1. find mutexes in plan.. ordered pairs of actions
    // 2. plan actions with no mutexes at 0 and incrementally plan others after the max mutex of previous ones (DAG)
    // Mutexes:
    // * Actions of the same vehicle
    // * drop/pick-up of the same package
    protected static TemporalPlan schedule(Problem temporalProblem, Collection<TemporalPlanAction> seqActions) {
        Set<TemporalPlanAction> tempActions = new HashSet<>();
        return new TemporalPlan(seqActions);
    }

    // remove all fuel-related stuff from vehicles and graph roads
    // TODO: verify that all seq. planners use package size correctly
    protected static Problem translateToSequential(Problem problem) {
        Iterator<Vehicle> newVehicles = problem.getAllVehicles().stream().map(v -> v.updateCurFuelCapacity(null).updateMaxFuelCapacity(null)).iterator();
        Problem seqProblem = problem;
        while (newVehicles.hasNext()) {
            Vehicle vehicle = newVehicles.next();
            seqProblem = seqProblem.putVehicle(vehicle.getName(), vehicle);
        }

        RoadGraph graph = problem.getRoadGraph();
        RoadGraph newGraph = new DefaultRoadGraph(graph.getId());
        graph.getAllLocations().map(l -> l.updateHasPetrolStation(false)).forEach(newGraph::addLocation);
        Map<String, Location> newLocMap = Stream.ofAll(newGraph.getAllLocations().map(l -> Tuple.of(l.getName(), l))
                .collect(Collectors.toList())).toJavaMap(v -> v);
        graph.getAllRoads().forEach(re -> newGraph.addRoad(new DefaultRoad(re.getRoad().getName(), re.getRoad().getLength()),
                newLocMap.get(re.getFrom().getName()), newLocMap.get(re.getTo().getName())));
        return new DefaultProblem(problem.getName(), newGraph, seqProblem.getVehicleMap(), seqProblem.getPackageMap());
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
