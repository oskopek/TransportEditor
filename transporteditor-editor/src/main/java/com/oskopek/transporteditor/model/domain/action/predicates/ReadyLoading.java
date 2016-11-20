package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

public class ReadyLoading extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        return true; // built in
    }
}
