/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.functions;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.Vehicle;

public class Capacity extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Vehicle.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("Capacity can only be applied to one argument of type Vehicle");
        }
        return apply((Vehicle) actionObjects[0]);
    }

    public ActionCost apply(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        return vehicle.getCurCapacity();
    }

}
