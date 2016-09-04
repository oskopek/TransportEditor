/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

public class IsRoad extends DefaultPredicate {

    @Override
    public boolean isValid(Problem state, Action action) {
        String location1 = action.getWhere().getName();
        String location2 = action.getWhat().getName();
        return state.getRoadGraph().getNode(location1).hasEdgeToward(location2);
    }
}
