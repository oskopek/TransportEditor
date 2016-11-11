/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Problem;

public class WhoAtWhat extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Locatable who = state.getLocatable(action.getWho().getName());
        Locatable what = state.getLocatable(action.getWhat().getName());
        return who.getLocation().equals(what.getLocation());
    }
}
