package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.RandomizedRestartWithAroundPathPickupPlanner;

import java.util.Optional;

public class RRWASequentialScheduler extends SequentialScheduler {

    private final AbstractPlanner planner = new RandomizedRestartWithAroundPathPickupPlanner();

    @Override
    protected Optional<Plan> planInternal(Domain seqDomain, Problem seqProblem) {
        return planner.plan(seqDomain, seqProblem);
    }

    @Override
    public RRWASequentialScheduler copy() {
        return new RRWASequentialScheduler();
    }
}
