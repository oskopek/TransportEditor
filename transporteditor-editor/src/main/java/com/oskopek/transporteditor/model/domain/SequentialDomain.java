/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.model.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;

import java.util.Arrays;
import java.util.List;

public class SequentialDomain extends DefaultDomain {

    private final List<Predicate> predicateList = Arrays.asList(new At(), new HasCapacity(), new In(), new IsRoad());

    private final List<Function> functionList = Arrays.asList(new RoadLength(), new TotalCost());

    public SequentialDomain(String name) {
        super(name, new DriveBuilder(Arrays.asList(new At()), Arrays.asList(), ));
    }

    @Override
    public List<Predicate> getPredicateList() {
        return predicateList;
    }

    @Override
    public List<Function> getFunctionList() {
        return functionList;
    }

    @Override
    public DomainLabel getDomainType() {
        return DomainLabel.ActionCost;
    }
}
