/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import com.oskopek.transporteditor.planning.plan.visualization.PlanState;

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
    public boolean isValid(PlanState planState) {
        return !internal.isValid(planState);
    }
}
