package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import javaslang.collection.Stream;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public abstract class SequentialRandomizedPlanner extends AbstractPlanner {

    private ArrayTable<String, String, ShortestPath> shortestPathMatrix;
    private Random random;
    private Plan bestPlan;
    private int bestPlanScore;

    protected ArrayTable<String, String, ShortestPath> getShortestPathMatrix() {
        return shortestPathMatrix;
    }

    protected Random getRandom() {
        return random;
    }

    protected Plan getBestPlan() {
        return bestPlan;
    }

    protected int getBestPlanScore() {
        return bestPlanScore;
    }

    protected void setBestPlan(Plan bestPlan) {
        this.bestPlan = bestPlan;
    }

    protected void setBestPlanScore(int bestPlanScore) {
        this.bestPlanScore = bestPlanScore;
    }

    protected void resetState() {
        random = null;
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    protected void initialize(Problem problem) {
        shortestPathMatrix = PlannerUtils.computeAPSP(problem.getRoadGraph());
        random = new Random(2017L);
    }

    protected Optional<Vehicle> nearestVehicle(Collection<Vehicle> vehicles, Location curLocation, int minFreeCapacity) {
        return Stream.ofAll(vehicles).filter(v -> v.getCurCapacity().getCost() >= minFreeCapacity)
                .minBy(v -> getShortestPathMatrix().get(v.getLocation().getName(), curLocation.getName()).getDistance())
                .toJavaOptional();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
}
