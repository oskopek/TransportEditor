package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * An abstraction over Weighted A* forward search, using exponential decay of the weight.
 */
public abstract class MetaSFAPlanner extends ForwardAstarPlanner {

    private Integer weight;

    private Plan bestPlan;
    private int bestPlanScore;

    /**
     * Default constructor.
     */
    public MetaSFAPlanner() {
        super(true);
        setName(MetaSFAPlanner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    void resetBestPlan() {
        // Intentionally do not reset: super.resetBestPlan();
    }

    /**
     * Get the planner who's heuristic we are referencing.
     *
     * @return the internal planner
     */
    protected abstract ForwardAstarPlanner getHeuristicReferencePlanner();

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem, Function<Plan, Plan> planTransformation) {
        weight = 400;
        final float coef = 0.5f;
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;

        resetState();
        initialize(problem);
        while (true) {
            if (weight == 1) {
                formatLog("Will not stop at first solution anymore, weight is {}.", weight);
                setStopAtFirstSolution(false);
            }
            formatLog("Setting weight to {}.", weight);
            Optional<Plan> plan = super.planInternal(domain, problem, planTransformation);
            plan.ifPresent(plan2 -> {
                Double makespan = plan2.calculateMakespan();
                if (makespan < bestPlanScore) {
                    formatLog("Found new best plan (weight: {}, score: {})", weight, makespan);
                    bestPlan = plan2;
                    bestPlanScore = Math.round(makespan.floatValue());
                }
            });
            if (shouldCancel() || weight == 1) { // plan ended with weight == 1
                formatLog("Cancelling, returning WASTAR best plan so far with score: {}.", bestPlanScore);
                return Optional.ofNullable(bestPlan);
            }
            float updatedWeight = coef * weight;
            int newWeight = Math.max(1, Math.round(updatedWeight));
            if (newWeight != weight) {
                weight = newWeight;
                recalculateOpenStateValues();
            }
        }
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Collection<Package> unfinishedPackages) {
        return weight * getHeuristicReferencePlanner().calculateHeuristic(state, distanceMatrix, unfinishedPackages);
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaSFAPlanner;
    }

}
