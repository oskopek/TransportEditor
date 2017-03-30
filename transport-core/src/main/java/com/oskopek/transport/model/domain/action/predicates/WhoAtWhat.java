/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Locatable;
import com.oskopek.transport.model.problem.Problem;

/**
 * Asserts whether the locatable (who) is at the same place as a given locatable (where).
 */
public class WhoAtWhat extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Locatable who = state.getLocatable(action.getWho().getName());
        Locatable what = state.getLocatable(action.getWhat().getName());
        return who.getLocation().equals(what.getLocation());
    }
}
