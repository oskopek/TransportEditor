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

import java.util.Map;
import java.util.Set;

/**
 * Represents the actual specific Transport domain, with all the constraints, parameters and objective functions
 * fully specified.
 * <p>
 * The domain also knows how to export itself (with all the technicalities) as a valid PDDL file and export (valid)
 * plans created in this domain.
 */
public interface Domain {

    String getName();

    Map<String, Class<? extends Predicate>> getPredicateMap();

    Map<String, Class<? extends Function>> getFunctionMap();

    DriveBuilder buildDrive();

    DropBuilder buildDrop();

    PickUpBuilder buildPickUp();

    RefuelBuilder buildRefuel();

    Set<DomainLabel> getDomainLabels();

}
