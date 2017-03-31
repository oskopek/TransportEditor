/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Locatable;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Problem;

/**
 * Asserts whether the locatable (what) is at given location (where).
 */
public class WhatAtWhere extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Locatable what = state.getLocatable(action.getWhat().getName());
        Location where = action.getWhere();
        return where.equals(what.getLocation());
    }
}
