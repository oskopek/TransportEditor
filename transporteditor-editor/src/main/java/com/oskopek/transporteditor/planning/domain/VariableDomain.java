/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;

import java.util.List;

public class VariableDomain implements Domain {

    @Override
    public List<Predicate> getPredicates() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<Function> getFunctions() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
