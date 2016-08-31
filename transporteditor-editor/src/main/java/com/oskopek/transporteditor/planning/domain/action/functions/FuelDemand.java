/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.functions;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.FuelRoad;

public class FuelDemand implements Function {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !FuelRoad.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("FuelDemand can only be applied to one argument of type FuelRoad");
        }
        return apply((FuelRoad) actionObjects[0]);
    }

    public ActionCost apply(FuelRoad road) {
        if (road == null) {
            throw new IllegalArgumentException("FuelRoad cannot be null.");
        }
        return road.getFuelCost();
    }

}
