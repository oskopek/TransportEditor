/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.plan.visualization.PlanState;
import com.oskopek.transporteditor.planning.problem.Problem;

public class HasPetrolStation extends DefaultPredicate {

    public HasPetrolStation(TemporalQuantifier quantifier) {
        super(quantifier);
    }

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan, PlanState planState) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
