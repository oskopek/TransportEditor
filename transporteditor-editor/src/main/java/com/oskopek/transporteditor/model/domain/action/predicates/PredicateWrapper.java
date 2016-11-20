package com.oskopek.transporteditor.model.domain.action.predicates;

public abstract class PredicateWrapper extends DefaultPredicate {

    private final Predicate internal;

    public PredicateWrapper(Predicate internal) {
        super();
        if (internal == null) {
            throw new IllegalArgumentException("Internal predicate cannot be null.");
        }
        this.internal = internal;
    }

    protected Predicate getInternal() {
        return internal;
    }
}
