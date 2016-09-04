/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class Drive extends DefaultAction<Vehicle, Location> {

    public Drive(Vehicle vehicle, Location location, Location location2, List<Predicate> preconditions,
            List<Predicate> effects, Road road) {
        super("drive", vehicle, location, location2, preconditions, effects, road.getLength(), road.getLength());
    }
}
