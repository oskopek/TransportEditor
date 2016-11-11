/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.FuelVehicle;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.PlaceholderActionObject;
import com.oskopek.transporteditor.model.problem.Problem;

import java.util.List;

public class Refuel extends DefaultAction<FuelVehicle, PlaceholderActionObject> {

    public Refuel(FuelVehicle vehicle, Location location,
            List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("refuel", vehicle, location, new PlaceholderActionObject(), preconditions, effects, cost, duration);
    }

    @Override
    public Problem apply(Domain domain, Problem problemState) {
        String name = this.getWho().getName();
        FuelVehicle vehicle = (FuelVehicle) problemState.getVehicle(name);
        return problemState.updateVehicle(name, vehicle.updateCurFuelCapacity(vehicle.getMaxFuelCapacity()));
    }
}
