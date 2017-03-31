package com.oskopek.transport.model.domain.action.predicates;

/**
 * A predicate wrapper. Wraps a given predicate and makes it accessible internally. Useful for nested predicates.
 */
public abstract class PredicateWrapper extends DefaultPredicate {

    private final Predicate internal;

    /**
     * Default constructor.
     *
     * @param internal the internal non-null predicate to store
     */
    public PredicateWrapper(Predicate internal) {
        super();
        if (internal == null) {
            throw new IllegalArgumentException("Internal predicate cannot be null.");
        }
        this.internal = internal;
    }

    /**
     * Get the internal predicate.
     *
     * @return the internal predicate
     */
    protected Predicate getInternal() {
        return internal;
    }
}
