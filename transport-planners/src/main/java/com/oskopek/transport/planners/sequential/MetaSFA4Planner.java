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
public final class MetaSFA4Planner extends MetaSFAPlanner {

    private SFA4Planner heuristicReferencePlanner;

    /**
     * Default constructor.
     */
    public MetaSFA4Planner() {
        setName(MetaSFA4Planner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
        heuristicReferencePlanner = new SFA4Planner();
    }

    @Override
    protected SFA4Planner getHeuristicReferencePlanner() {
        return heuristicReferencePlanner;
    }

    @Override
    public MetaSFA4Planner copy() {
        return new MetaSFA4Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaSFA4Planner;
    }

}
