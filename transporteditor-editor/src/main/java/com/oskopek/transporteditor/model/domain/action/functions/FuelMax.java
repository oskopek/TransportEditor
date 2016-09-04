/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.FuelVehicle;

public class FuelMax extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !FuelVehicle.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("FuelMax can only be applied to one argument of type FuelVehicle.");
        }
        return apply((FuelVehicle) actionObjects[0]);
    }

    public ActionCost apply(FuelVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("FuelVehicle cannot be null.");
        }
        return vehicle.getMaxFuelCapacity();
    }

}
