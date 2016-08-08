/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.planstate.PlanState;
import com.oskopek.transporteditor.planning.problem.Problem;

public interface Predicate {

    boolean isValid(Domain domain, Problem problem, Plan plan, PlanState planState);

    /**
     * Temporal quantifier passed as part of the state of the predicate, not it's parameter.
     *
     * @return non-null
     */
    TemporalQuantifier getTemporalQuantifier();

}
