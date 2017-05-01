package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * SFA* with
 * {@link PlannerUtils#calculateSumOfDistancesToVehiclesPackageTargetsAdmissible(Collection, Collection, ArrayTable)}
 * as a heuristic.
 * Acts as a Weighted A* with a metaheuristically decreasing weight.
 */
public final class MetaSFA5Planner extends MetaSFAPlanner {

    private SFA5Planner heuristicReferencePlanner;

    /**
     * Default constructor.
     */
    public MetaSFA5Planner() {
        setName(MetaSFA5Planner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
        heuristicReferencePlanner = new SFA5Planner();
    }

    @Override
    protected SFA5Planner getHeuristicReferencePlanner() {
        return heuristicReferencePlanner;
    }

    @Override
    public MetaSFA5Planner copy() {
        return new MetaSFA5Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaSFA5Planner;
    }

}
