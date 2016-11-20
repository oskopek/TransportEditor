package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

public class Not extends PredicateWrapper {

    public Not(Predicate internal) {
        super(internal);
    }

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        return !getInternal().isValid(state, action);
    }

    @Override
    public String getPredicateName() {
        return "!(" + getInternal().getPredicateName() + ")";
    }
}
