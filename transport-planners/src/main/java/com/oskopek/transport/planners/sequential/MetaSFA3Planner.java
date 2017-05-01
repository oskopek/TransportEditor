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
public final class MetaSFA3Planner extends MetaSFAPlanner {

    private SFA3Planner heuristicReferencePlanner;

    /**
     * Default constructor.
     */
    public MetaSFA3Planner() {
        setName(MetaSFA3Planner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
        heuristicReferencePlanner = new SFA3Planner();
    }

    @Override
    protected SFA3Planner getHeuristicReferencePlanner() {
        return heuristicReferencePlanner;
    }

    @Override
    public MetaSFA3Planner copy() {
        return new MetaSFA3Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaSFA3Planner;
    }

}
