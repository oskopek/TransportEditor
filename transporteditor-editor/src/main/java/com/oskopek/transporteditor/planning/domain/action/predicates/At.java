/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import com.oskopek.transporteditor.planning.domain.action.Action;
import com.oskopek.transporteditor.planning.plan.visualization.PlanState;
import com.oskopek.transporteditor.planning.problem.Locatable;
import com.oskopek.transporteditor.planning.problem.Location;

public class At extends DefaultPredicate {

    public At(TemporalQuantifier quantifier) {
        super(quantifier);
    }

    @Override
    public boolean isValid(PlanState planState) {
        Action action = planState.getTimePoint().getPlanEntry().getAction();
        Locatable who = (Locatable) planState.getCurrentActionObjects().get(action.getWho().getName());
        Location where = (Location) planState.getCurrentActionObjects().get(
                action.getWhere().getName()); // TODO: What about type safety here?
        return who.getLocation().equals(where);
    }
}
