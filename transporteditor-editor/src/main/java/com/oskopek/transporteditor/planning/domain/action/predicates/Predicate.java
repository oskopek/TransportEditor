/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import com.oskopek.transporteditor.planning.plan.visualization.PlanState;

public interface Predicate {

    boolean isValid(PlanState planState);

    /**
     * Temporal quantifier passed as part of the state of the predicate, not it's parameter.
     *
     * @return non-null
     */
    TemporalQuantifier getTemporalQuantifier();

}
