/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.plan.visualization.PlanState;
import com.oskopek.transporteditor.planning.problem.Problem;

public class Not implements Predicate {

    private final Predicate internal;

    public Not(Predicate internal) {
        this.internal = internal;
    }

    @Override
    public TemporalQuantifier getTemporalQuantifier() {
        return internal.getTemporalQuantifier();
    }

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan, PlanState planState) {
        return !internal.isValid(domain, problem, plan, planState);
    }
}
