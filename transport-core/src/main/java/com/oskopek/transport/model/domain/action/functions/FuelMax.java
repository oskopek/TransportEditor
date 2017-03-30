package com.oskopek.transport.model.domain.action.functions;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Vehicle;

/**
 * The fuel-max function. Returns the the maximum possible fuel capacity that the {@link Vehicle} has.
 */
public class FuelMax extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Vehicle.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("FuelMax can only be applied to one argument of type Vehicle.");
        }
        return apply((Vehicle) actionObjects[0]);
    }

    /**
     * Get the maximum fuel capacity of the {@link Vehicle}.
     *
     * @param vehicle the vehicle
     * @return the maximum fuel capacity of the vehicle
     */
    public ActionCost apply(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        return vehicle.getMaxFuelCapacity();
    }

}
