package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Problem;

/**
 * Negation predicate. Negates the validity of the internal predicate.
 */
public class Not extends PredicateWrapper {

    /**
     * Default constructor.
     *
     * @param internal the internal predicate
     */
    public Not(Predicate internal) {
        super(internal);
    }

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        return !getInternal().isValid(state, action);
    }

    @Override
    public String getPredicateName() {
        return "!(" + getInternal().getPredicateName() + ')';
    }
}
