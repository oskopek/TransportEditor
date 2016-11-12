package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;

import java.util.Map;
import java.util.Set;

public class VariableDomain extends DefaultDomain {

    private final Map<String, Class<? extends Predicate>> predicateMap;
    private final Map<String, Class<? extends Function>> functionMap;

    public VariableDomain(String name, DriveBuilder driveBuilder, DropBuilder dropBuilder, PickUpBuilder pickUpBuilder,
            RefuelBuilder refuelBuilder, Set<PddlLabel> pddlLabelSet,
            Map<String, Class<? extends Predicate>> predicateMap, Map<String, Class<? extends Function>> functionMap) {
        super(name, driveBuilder, dropBuilder, pickUpBuilder, refuelBuilder, pddlLabelSet);
        this.predicateMap = predicateMap;
        this.functionMap = functionMap;
    }

    @Override
    public Map<String, Class<? extends Predicate>> getPredicateMap() {
        return predicateMap;
    }

    @Override
    public Map<String, Class<? extends Function>> getFunctionMap() {
        return functionMap;
    }
}
