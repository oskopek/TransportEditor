/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.predicates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class DefaultPredicate implements Predicate {

    private final TemporalQuantifier quantifier;

    public DefaultPredicate(TemporalQuantifier quantifier) {
        this.quantifier = quantifier;
    }

    @Override
    public TemporalQuantifier getTemporalQuantifier() {
        return quantifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultPredicate)) {
            return false;
        }
        DefaultPredicate that = (DefaultPredicate) o;
        return new EqualsBuilder().append(getClass(), that.getClass()).append(quantifier, that.quantifier).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getClass()).append(quantifier).toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "quantifier=" + quantifier + ']';
    }
}
