package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Vehicle;

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
