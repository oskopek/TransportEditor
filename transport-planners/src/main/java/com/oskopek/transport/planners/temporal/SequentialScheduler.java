package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.AbstractPlanner;

import java.util.Optional;

public abstract class SequentialScheduler extends AbstractPlanner {

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        SequentialDomain seqDomain = new SequentialDomain(domain.getName() + "-seq");
        Problem seqProblem = translateToSequential(domain, problem);
        Optional<Plan> sequentialPlan = planInternal(seqDomain, seqProblem);
        return sequentialPlan.map(plan -> schedule(seqProblem, plan));
    }

    protected abstract Optional<Plan> planInternal(Domain seqDomain, Problem seqProblem);

    // 1. find mutexes in plan.. ordered pairs of actions
    // 2. plan actions with no mutexes at 0 and incrementally plan others after the max mutex of previous ones (DAG)
    // Mutexes:
    // * Actions of the same vehicle
    // * drop/pick-up of the same package
    private static Plan schedule(Problem seqProblem, Plan seqPlan) {
        return null;
    }

    // remove all fuel-related stuff from vehicles and graph roads
    // TODO: verify that all seq. planners use package size correctly
    private static Problem translateToSequential(Domain domain, Problem problem) {
        return null;
    }

    @Override
    public abstract SequentialScheduler copy();
}
