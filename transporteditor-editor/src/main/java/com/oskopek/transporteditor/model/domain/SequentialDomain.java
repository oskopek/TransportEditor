/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.model.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SequentialDomain extends DefaultDomain {

    private final List<Predicate> predicateList = Arrays.asList(new At(), new HasCapacity(), new In(), new IsRoad());

    private final List<Function> functionList = Arrays.asList(new RoadLength(), new TotalCost());

    public SequentialDomain(String name) {
        super(name, new DriveBuilder(Arrays.asList(new At(), new IsRoad()), Arrays.asList(new Not(new At()), new At())),
                new DropBuilder(Arrays.asList(new At(), new In()), Arrays.asList(new At(), new Not(new In())),
                        ActionCost.valueOf(1), ActionCost.valueOf(1)),
                new PickUpBuilder(Arrays.asList(new At(), new At(), new HasCapacity()),
                        Arrays.asList(new Not(new At()), new In()), ActionCost.valueOf(1), ActionCost.valueOf(1)), null,
                new HashSet<>(Arrays.asList(DomainLabel.ActionCost, DomainLabel.Capacity, DomainLabel.MaxCapacity,
                        DomainLabel.MinimizeDuration)));
    }

    @Override
    public List<Predicate> getPredicateList() {
        return predicateList;
    }

    @Override
    public List<Function> getFunctionList() {
        return functionList;
    }
}
