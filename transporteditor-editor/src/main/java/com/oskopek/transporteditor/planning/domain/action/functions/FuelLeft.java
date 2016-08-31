/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.functions;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.FuelVehicle;

public class FuelLeft implements Function {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !FuelVehicle.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("FuelLeft can only be applied to one argument of type FuelVehicle.");
        }
        return apply((FuelVehicle) actionObjects[0]);
    }

    public ActionCost apply(FuelVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("FuelVehicle cannot be null.");
        }
        return vehicle.getCurFuelCapacity();
    }

}
