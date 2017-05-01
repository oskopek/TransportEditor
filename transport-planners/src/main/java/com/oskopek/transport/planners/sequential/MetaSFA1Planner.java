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
public final class MetaSFA1Planner extends MetaSFAPlanner {

    private SFA1Planner heuristicReferencePlanner;

    /**
     * Default constructor.
     */
    public MetaSFA1Planner() {
        setName(MetaSFA1Planner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
        heuristicReferencePlanner = new SFA1Planner();
    }

    @Override
    protected SFA1Planner getHeuristicReferencePlanner() {
        return heuristicReferencePlanner;
    }

    @Override
    public MetaSFA1Planner copy() {
        return new MetaSFA1Planner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaSFA1Planner;
    }

}
