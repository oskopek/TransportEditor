package com.oskopek.transport.model.domain.action.functions;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Vehicle;

/**
 * The capacity function. Returns the the current capacity that the {@link Vehicle} has left.
 */
public class Capacity extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Vehicle.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("Capacity can only be applied to one argument of type Vehicle");
        }
        return apply((Vehicle) actionObjects[0]);
    }

    /**
     * Get the current capacity of the {@link Vehicle}.
     *
     * @param vehicle the vehicle
     * @return the current capacity of the vehicle
     */
    public ActionCost apply(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        return vehicle.getCurCapacity();
    }

}
