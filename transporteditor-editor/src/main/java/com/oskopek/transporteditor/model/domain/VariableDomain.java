/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;

import java.util.List;
import java.util.Set;

public class VariableDomain extends DefaultDomain {

    private final List<Predicate> predicateList;
    private final List<Function> functionList;

    public VariableDomain(String name, DriveBuilder driveBuilder, DropBuilder dropBuilder, PickUpBuilder pickUpBuilder,
            RefuelBuilder refuelBuilder, Set<DomainLabel> domainLabelSet, List<Predicate> predicateList,
            List<Function> functionList) {
        super(name, driveBuilder, dropBuilder, pickUpBuilder, refuelBuilder, domainLabelSet);
        this.predicateList = predicateList;
        if (this.predicateList != null) {
            this.predicateList.sort((o1, o2) -> o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName()));
        }
        this.functionList = functionList;
        if (this.predicateList != null) {
            this.functionList.sort((o1, o2) -> o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName()));
        }
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
