/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.Drop;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.domain.action.Refuel;
import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;

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

    DriveBuilder getDriveBuilder();

    DropBuilder getDropBuilder();

    PickUpBuilder getPickUpBuilder();

    RefuelBuilder getRefuelBuilder();

    Drive buildDrive(Vehicle vehicle, Location from, Location to, Road road);

    Drive buildDrive(Vehicle vehicle, Location from, Location to, RoadGraph graph);

    Drop buildDrop(Vehicle vehicle, Location at, Package what);

    PickUp buildPickUp(Vehicle vehicle, Location at, Package what);

    Refuel buildRefuel(Vehicle vehicle, Location at);

    Set<PddlLabel> getPddlLabels();

}
