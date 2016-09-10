/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.PlaceholderActionObject;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class Refuel extends DefaultAction<Vehicle, PlaceholderActionObject> {

    public Refuel(Vehicle vehicle, Location location,
            List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("refuel", vehicle, location, new PlaceholderActionObject(), preconditions, effects, cost, duration);
    }
}