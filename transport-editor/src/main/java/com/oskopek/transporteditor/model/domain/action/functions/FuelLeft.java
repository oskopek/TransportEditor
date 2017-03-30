package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Vehicle;

/**
 * The fuel-left function. Returns the the current fuel capacity that the {@link Vehicle} has left.
 */
public class FuelLeft extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Vehicle.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("FuelLeft can only be applied to one argument of type Vehicle.");
        }
        return apply((Vehicle) actionObjects[0]);
    }

    /**
     * Get the current fuel capacity of the {@link Vehicle}.
     *
     * @param vehicle the vehicle
     * @return the current fuel capacity of the vehicle
     */
    public ActionCost apply(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        return vehicle.getCurFuelCapacity();
    }

}
