/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;

import java.util.List;

public class VariableDomain extends DefaultDomain {

    private final List<Class<? extends Predicate>> predicateList;
    private final List<Class<? extends Function>> functionList;

    private final DomainType domainType;

    public VariableDomain(DomainType type, List<Class<? extends Predicate>> predicateList,
            List<Class<? extends Function>> functionList) {
        this.domainType = type;
        this.predicateList = predicateList;
        if (this.predicateList != null) {
            this.predicateList.sort((o1, o2) -> o1.getSimpleName().compareTo(o2.getSimpleName()));
        }
        this.functionList = functionList;
        if (this.predicateList != null) {
            this.functionList.sort((o1, o2) -> o1.getSimpleName().compareTo(o2.getSimpleName()));
        }
    }

    @Override
    public List<Class<? extends Predicate>> getPredicateList() {
        return predicateList;
    }

    @Override
    public List<Class<? extends Function>> getFunctionList() {
        return functionList;
    }

    @Override
    public DomainType getDomainType() {
        return domainType;
    }
}
