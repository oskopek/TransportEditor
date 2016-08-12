/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates.sequential;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.domain.action.predicates.SequentialPredicate;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.plan.visualization.PlanState;
import com.oskopek.transporteditor.planning.problem.Problem;

public class In extends SequentialPredicate {

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan, PlanState planState) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
