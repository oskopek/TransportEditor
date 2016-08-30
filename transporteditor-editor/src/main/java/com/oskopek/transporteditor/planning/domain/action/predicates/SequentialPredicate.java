/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

public abstract class SequentialPredicate implements Predicate {

    @Override
    public TemporalQuantifier getTemporalQuantifier() {
        return null;
    }
}
