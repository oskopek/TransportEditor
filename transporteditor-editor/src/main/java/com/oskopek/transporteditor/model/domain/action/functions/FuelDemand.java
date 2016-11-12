package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.FuelRoad;

public class FuelDemand extends DefaultFunction {

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
