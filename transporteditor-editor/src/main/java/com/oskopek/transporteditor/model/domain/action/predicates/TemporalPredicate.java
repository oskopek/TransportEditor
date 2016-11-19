package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalQuantifier;
import com.oskopek.transporteditor.model.problem.Problem;

public class TemporalPredicate extends PredicateWrapper {

    private final TemporalQuantifier quantifier;

    public TemporalPredicate(Predicate internal) {
        this(internal, TemporalQuantifier.OVER_ALL);
    }

    public TemporalPredicate(Predicate internal, TemporalQuantifier quantifier) {
        super(internal);
        this.quantifier = quantifier;
    }

    @Override
    public boolean isValid(Problem state, Action action) {
        return getInternal().isValid(state, action);
    }

    @Override
    public TemporalQuantifier getTemporalQuantifier() {
        return quantifier;
    }
}
