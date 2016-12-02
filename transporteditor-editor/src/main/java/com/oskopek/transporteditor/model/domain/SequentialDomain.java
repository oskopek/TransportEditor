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
import com.oskopek.transporteditor.model.domain.builder.DomainConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequentialDomain extends DefaultDomain {



    public SequentialDomain(String name) {
        super(name, new DriveBuilder(DomainConstants.Sequential.drivePreconditions, DomainConstants.Sequential.driveEffects),
                new DropBuilder(DomainConstants.Sequential.dropPreconditions, DomainConstants.Sequential.dropEffects, ActionCost.valueOf(1), ActionCost.valueOf(1)),
                new PickUpBuilder(DomainConstants.Sequential.pickUpPreconditions, DomainConstants.Sequential.pickUpEffects, ActionCost.valueOf(1),
                        ActionCost.valueOf(1)), null,
                ImmutableSet.of(PddlLabel.ActionCost, PddlLabel.Capacity, PddlLabel.MaxCapacity));
    }

    @Override
    public Map<String, Class<? extends Predicate>> getPredicateMap() {
        return DomainConstants.Sequential.predicateMap;
    }

    @Override
    public Map<String, Class<? extends Function>> getFunctionMap() {
        return DomainConstants.Sequential.functionMap;
    }
}
