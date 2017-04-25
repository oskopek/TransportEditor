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
public class SFA4Planner extends ForwardAstarPlanner {

    /**
     * Default constructor.
     */
    public SFA4Planner() {
        super(false);
        setName(SFA4Planner.class.getSimpleName());
    }

    /**
     * Default constructor.
     *
     * @param stopAtFirstSolution true iff the algorithm should stop after the first found solution
     */
    public SFA4Planner(boolean stopAtFirstSolution) {
        super(stopAtFirstSolution);
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Collection<Package> unfinishedPackages) {
        return PlannerUtils.calculateSumOfDistancesToVehiclesPackageTargetsAdmissibleReally(unfinishedPackages,
                state.getProblem().getAllVehicles(), distanceMatrix);
    }

    @Override
    public SFA4Planner copy() {
        return new SFA4Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SFA4Planner;
    }

}
