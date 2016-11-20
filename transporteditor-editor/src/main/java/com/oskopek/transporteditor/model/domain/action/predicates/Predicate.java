package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalQuantifier;
import com.oskopek.transporteditor.model.problem.Problem;

public interface Predicate {

    boolean isValid(Problem state, Action action);

    default TemporalQuantifier getTemporalQuantifier() {
        return TemporalQuantifier.OVER_ALL;
    }

    default String getPredicateName() {
        return getClass().getSimpleName() + "[" + getTemporalQuantifier().name() + "]";
    }

}
