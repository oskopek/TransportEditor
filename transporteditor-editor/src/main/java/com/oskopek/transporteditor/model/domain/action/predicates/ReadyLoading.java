/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

public class ReadyLoading extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        throw new UnsupportedOperationException(
                "Not implemented yet."); // TODO: Not sure what should be done here. remove it?s
    }
}
