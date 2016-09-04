/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;

import java.util.List;

public class VariableDomain extends DefaultDomain {

    private final List<Predicate> predicateList;
    private final List<Function> functionList;

    private final DomainLabel domainLabel;

    public VariableDomain(DomainLabel type, List<Predicate> predicateList, List<Function> functionList) {
        this.domainLabel = type;
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

    public DomainLabel getDomainLabel() {
        return domainLabel;
    }
}
