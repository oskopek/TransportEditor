/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.*;
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

    List<Class<? extends Action>> getActionList();

    Drive getDriveAction();

    Drop getDropAction();

    PickUp getPickUpAction();

    Class<? extends Refuel> getRefuelAction();

    DomainType getDomainType();

}
