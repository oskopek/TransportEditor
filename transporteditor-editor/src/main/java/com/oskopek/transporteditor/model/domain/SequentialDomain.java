/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.google.common.collect.ImmutableSet;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.model.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SequentialDomain extends DefaultDomain {

    private final static Map<String, Class<? extends Predicate>> predicateMap;
    private final static Map<String, Class<? extends Function>> functionMap;

    public SequentialDomain(String name) {
        super(name, new DriveBuilder(Arrays.asList(new At(), new IsRoad()), Arrays.asList(new Not(new At()), new At())),
                new DropBuilder(Arrays.asList(new At(), new In()), Arrays.asList(new At(), new Not(new In())),
                        ActionCost.valueOf(1), ActionCost.valueOf(1)),
                new PickUpBuilder(Arrays.asList(new At(), new At(), new HasCapacity()),
                        Arrays.asList(new Not(new At()), new In()), ActionCost.valueOf(1), ActionCost.valueOf(1)), null,
                ImmutableSet.of(DomainLabel.ActionCost, DomainLabel.Capacity, DomainLabel.MaxCapacity));
    }

    @Override
    public Map<String, Class<? extends Predicate>> getPredicateMap() {
        return predicateMap;
    }

    @Override
    public Map<String, Class<? extends Function>> getFunctionMap() {
        return functionMap;
    }

    static {
        predicateMap = new HashMap<>(4);
        predicateMap.put("at", At.class);
        predicateMap.put("capacity", HasCapacity.class);
        predicateMap.put("in", In.class);
        predicateMap.put("road", IsRoad.class);
    }

    static {
        functionMap = new HashMap<>(2);
        functionMap.put("road-length", RoadLength.class);
        functionMap.put("total-cost", TotalCost.class);
    }
}
