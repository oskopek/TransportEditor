package com.oskopek.transporteditor.model.domain.action.predicates;

public abstract class PredicateWrapper implements Predicate {

    private final Predicate internal;

    public PredicateWrapper(Predicate internal) {
        this.internal = internal;
    }

    protected Predicate getInternal() {
        return internal;
    }
}
