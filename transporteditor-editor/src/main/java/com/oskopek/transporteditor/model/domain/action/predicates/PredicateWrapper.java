package com.oskopek.transporteditor.model.domain.action.predicates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class PredicateWrapper implements Predicate {

    private final Predicate internal;

    public PredicateWrapper(Predicate internal) {
        this.internal = internal;
    }

    protected Predicate getInternal() {
        return internal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PredicateWrapper)) {
            return false;
        }
        PredicateWrapper that = (PredicateWrapper) o;
        return new EqualsBuilder().append(getClass().getSimpleName(), that.getClass().getSimpleName()).append(
                getInternal(), that.getInternal()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getClass().getSimpleName()).append(getInternal()).toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
