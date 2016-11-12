package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

public class Not implements Predicate {

    private final Predicate internal;

    public Not(Predicate internal) {
        this.internal = internal;
    }

    @Override
    public boolean isValid(Problem state, Action action) {
        return !internal.isValid(state, action);
    }
}
