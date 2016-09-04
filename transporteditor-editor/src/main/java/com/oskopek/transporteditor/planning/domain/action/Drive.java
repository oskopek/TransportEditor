/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.problem.Location;
import com.oskopek.transporteditor.planning.problem.Vehicle;

import java.util.List;

public class Drive extends DefaultAction<Vehicle, Location, Location> {

    public Drive(Vehicle vehicle, Location location, Location location2, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("drive", vehicle, location, location2, preconditions, effects, cost, duration);
    }

}
