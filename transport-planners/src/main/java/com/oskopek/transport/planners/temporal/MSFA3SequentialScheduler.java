package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.MetaSFA3Planner;
import com.oskopek.transport.planners.sequential.RandomizedRestartAroundPathNearbyPlanner;
import com.oskopek.transport.tools.executables.LogListener;

import java.util.Optional;
import java.util.function.Function;

/**
 * Scheduler wrapper around {@link RandomizedRestartAroundPathNearbyPlanner}.
 */
public class MSFA3SequentialScheduler extends SequentialScheduler {

    private final AbstractPlanner planner = new MetaSFA3Planner();

    @Override
    public Optional<Plan> plan(Domain seqDomain, Problem seqProblem,
            Function<Plan, Plan> planTransformation) {
        LogListener listener = s -> this.log(s.trim());
        planner.subscribe(listener);
        Optional<Plan> plan = planner.plan(seqDomain, seqProblem, planTransformation);
        planner.unsubscribe(listener);
        return plan;
    }

    @Override
    protected AbstractPlanner getInternalPlanner() {
        return planner;
    }

    @Override
    public MSFA3SequentialScheduler copy() {
        return new MSFA3SequentialScheduler();
    }
}
