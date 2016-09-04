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

    private final static ActionCost cost = ActionCost.valueOf(1);
    private final static ActionCost duration = ActionCost.valueOf(1);

    public Drop(Vehicle vehicle, Location location, Package aPackage, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("drop", vehicle, location, aPackage, preconditions, effects, cost, duration);
    }

    public Drop(Vehicle vehicle, Location location, Package aPackage, ActionCost cost, ActionCost duration) {
        this(vehicle, location, aPackage, preconditions, effects, cost, duration);
    }

    public Drop(Vehicle vehicle, Location location, Package aPackage) {
        this(vehicle, location, aPackage, cost, duration);
    }
}
