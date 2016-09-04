/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.problem.Location;
import com.oskopek.transporteditor.planning.problem.Package;
import com.oskopek.transporteditor.planning.problem.Vehicle;

import java.util.List;

public class Drop extends DefaultAction<Vehicle, Location, Package> {

    public Drop(Vehicle vehicle, Location location, Package aPackage, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("drop", vehicle, location, aPackage, preconditions, effects, cost, duration);
    }
}
