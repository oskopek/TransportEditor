/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class Drop extends DefaultAction<Vehicle, Package> {

    public Drop(Vehicle vehicle, Location location, Package aPackage, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("drop", vehicle, location, aPackage, preconditions, effects, cost, duration);
    }

    @Override
    public Problem apply(Domain domain, Problem problemState) {
        throw new IllegalStateException("Not implemented yet.");
    }
}
