package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import javaslang.Function3;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * SFA* with
 * {@link PlannerUtils#calculateSumOfDistancesToVehiclesPackageTargetsAdmissible(Collection, Collection, ArrayTable)}
 * as a heuristic.
 * Acts as a Weighted A* with a metaheuristically decreasing weight.
 */
public final class MetaSFA4Planner extends SFA4Planner { // TODO: deduplicate

    private Function3<ImmutablePlanState, ArrayTable<String, String, ShortestPath>, Collection<Package>,
            Integer> weightedHeuristic;

    private Plan bestPlan;
    private int bestPlanScore;

    /**
     * Default constructor.
     */
    public MetaSFA4Planner() {
        super(true);
        setName(MetaSFA4Planner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    void resetBestPlan() {
        // Intentionally do not reset: super.resetBestPlan();
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        int weight = 200;
        final float coef = 0.5f;
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;

        while (true) {
            final int newWeight = weight;
            if (weight == 1) {
                formatLog("Will not stop at first solution anymore, weight is {}.", weight);
                setStopAtFirstSolution(false);
            }
            weightedHeuristic = (state, distanceMatrix, unfinishedPackages) -> newWeight * PlannerUtils
                    .calculateSumOfDistancesToVehiclesPackageTargetsAdmissibleReally(unfinishedPackages,
                            state.getProblem().getAllVehicles(), distanceMatrix);
            formatLog("Setting weight to {}.", weight);
            Optional<Plan> plan = super.plan(domain, problem);
            plan.ifPresent(plan2 -> {
                Double makespan = plan2.calculateMakespan();
                if (makespan < bestPlanScore) {
                    formatLog("Found new best plan (weight: {}, score: {})", newWeight, makespan);
                    bestPlan = plan2;
                    bestPlanScore = Math.round(makespan.floatValue());
                }
            });
            if (shouldCancel() || weight == 1) { // plan ended with weight == 1
                formatLog("Cancelling, returning WASTAR best plan so far with score: {}.", bestPlanScore);
                return Optional.ofNullable(bestPlan);
            }
            float updatedWeight = coef * weight;
            weight = Math.max(1, Math.round(updatedWeight));
        }
    }

    @Override
    protected Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Collection<Package> unfinishedPackages) {
        return weightedHeuristic.apply(state, distanceMatrix, unfinishedPackages);
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
