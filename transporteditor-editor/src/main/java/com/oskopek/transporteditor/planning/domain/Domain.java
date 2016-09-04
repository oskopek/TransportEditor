/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.Drive;
import com.oskopek.transporteditor.planning.domain.action.Drop;
import com.oskopek.transporteditor.planning.domain.action.PickUp;
import com.oskopek.transporteditor.planning.domain.action.Refuel;
import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;

import java.util.List;

/**
 * Represents the actual specific Transport domain, with all the constraints, parameters and objective functions
 * fully specified.
 * <p>
 * The domain also knows how to export itself (with all the technicalities) as a valid PDDL file and export (valid)
 * plans created in this domain.
 */
public interface Domain {

    String getName();

    List<Class<? extends Predicate>> getPredicateList();

    List<Class<? extends Function>> getFunctionList();

    Drive buildDrive();

    Drop buildDrop();

    PickUp buildPickUp();

    Refuel buildRefuel();

    DomainType getDomainType();

}
