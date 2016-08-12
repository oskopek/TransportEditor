/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.planning.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.planning.domain.action.predicates.IsRoad;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.domain.action.predicates.sequential.At;
import com.oskopek.transporteditor.planning.domain.action.predicates.sequential.Capacity;
import com.oskopek.transporteditor.planning.domain.action.predicates.sequential.In;
import com.oskopek.transporteditor.planning.plan.Plan;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class SequentialDomain implements Domain {

    private final List<Predicate> predicateList = Arrays.asList(new At(), new Capacity(), new In(), new IsRoad());

    private final List<Function> functionList = Arrays.asList(new TotalCost(), new RoadLength());

    @Override
    public List<? extends Predicate> getPredicates() {
        return predicateList;
    }

    @Override
    public List<? extends Function> getFunctions() {
        return functionList;
    }

    @Override
    public void toPDDLFormat(PrintWriter writer) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Domain fromPDDLFormat(String input) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void toVALFormat(PrintWriter writer, Plan plan) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Plan fromVALFormat(String input) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
