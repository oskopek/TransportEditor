/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.plan.Plan;

import java.io.PrintWriter;
import java.util.List;

public class SequentialDomain implements Domain { // TODO 1 Implement me

    @Override
    public List<Predicate> getPredicates() {
        return null;
    }

    @Override
    public List<Function> getFunctions() {
        return null;
    }

    @Override
    public void toPDDLFormat(PrintWriter writer) {

    }

    @Override
    public Domain fromPDDLFormat(String input) {
        return null;
    }

    @Override
    public void toVALFormat(PrintWriter writer, Plan plan) {

    }

    @Override
    public Plan fromVALFormat(String input) {
        return null;
    }
}
