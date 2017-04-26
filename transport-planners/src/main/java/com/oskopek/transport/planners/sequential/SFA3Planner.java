package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;

import java.util.Collection;

/**
 * SFA* with
 * {@link PlannerUtils#calculateSumOfDistancesToVehiclesPackageTargetsAdmissible(Collection, Collection, ArrayTable)}
 * as a heuristic.
 */
public class SFA3Planner extends ForwardAstarPlanner {

    /**
     * Default constructor.
     */
    public SFA3Planner() {
        super(false);
        setName(SFA3Planner.class.getSimpleName());
    }

    /**
     * Default constructor.
     *
     * @param stopAtFirstSolution true iff the algorithm should stop after the first found solution
     */
    public SFA3Planner(boolean stopAtFirstSolution) {
        super(stopAtFirstSolution);
        setName(SFA3Planner.class.getSimpleName());
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Collection<Package> unfinishedPackages) {
        return PlannerUtils.calculateSumOfDistancesToVehiclesPackageTargetsAdmissible(unfinishedPackages,
                state.getProblem().getAllVehicles(), distanceMatrix);
    }

    @Override
    public SFA3Planner copy() {
        return new SFA3Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SFA3Planner;
    }

}
