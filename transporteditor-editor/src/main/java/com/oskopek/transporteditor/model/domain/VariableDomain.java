package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;

import java.util.Map;
import java.util.Set;

/**
 * A data-oriented domain implementation. Very permissive and simple.
 */
public class VariableDomain extends DefaultDomain {

    private final Map<String, Class<? extends Predicate>> predicateMap;
    private final Map<String, Class<? extends Function>> functionMap;

    /**
     * Default constructor.
     *
     * @param name the name
     * @param driveBuilder the drive builder
     * @param dropBuilder the drop builder
     * @param pickUpBuilder the pick-up builder
     * @param refuelBuilder the refuel builder
     * @param pddlLabelSet the pddl labels
     * @param predicateMap the predicate map
     * @param functionMap  the function map
     */
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
