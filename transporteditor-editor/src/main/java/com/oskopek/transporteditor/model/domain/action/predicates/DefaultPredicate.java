/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.predicates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class DefaultPredicate implements Predicate {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultPredicate)) {
            return false;
        }
        DefaultPredicate that = (DefaultPredicate) o;
        return new EqualsBuilder().append(getClass(), that.getClass()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getClass()).toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
