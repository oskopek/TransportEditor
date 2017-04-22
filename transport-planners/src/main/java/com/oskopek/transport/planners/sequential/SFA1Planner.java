package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;

import java.util.Collection;

/**
 * SFA* with
 * {@link PlannerUtils#calculateSumOfDistancesToPackageTargets(Collection, Collection, ArrayTable)}
 * as a heuristic.
 */
public class SFA1Planner extends ForwardAstarPlanner {

    /**
     * Default constructor.
     */
    public SFA1Planner() {
        super(false);
        setName(SFA1Planner.class.getSimpleName());
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Collection<Package> unfinishedPackages) {
        return PlannerUtils.calculateSumOfDistancesToPackageTargets(unfinishedPackages,
                state.getProblem().getAllVehicles(), distanceMatrix);
    }

    @Override
    public SFA1Planner copy() {
        return new SFA1Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SFA1Planner;
    }
}
