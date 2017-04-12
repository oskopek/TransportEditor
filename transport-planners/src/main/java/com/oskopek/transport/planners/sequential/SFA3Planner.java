package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;

import java.util.Collection;

/**
 * Created by skopeko on 12.4.17.
 */
public class SFA3Planner extends ForwardAstarPlanner {

    public SFA3Planner() {
        setName(SFA3Planner.class.getSimpleName());
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state, ArrayTable<String, String, Integer> distanceMatrix,
            Collection<Package> unfinishedPackages) {
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
