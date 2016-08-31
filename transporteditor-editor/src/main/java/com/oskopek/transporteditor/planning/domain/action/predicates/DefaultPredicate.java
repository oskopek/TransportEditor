/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

public abstract class DefaultPredicate implements Predicate {

    private final TemporalQuantifier quantifier;

    public DefaultPredicate(TemporalQuantifier quantifier) {
        this.quantifier = quantifier;
    }

    @Override
    public TemporalQuantifier getTemporalQuantifier() {
        return quantifier;
    }
}
