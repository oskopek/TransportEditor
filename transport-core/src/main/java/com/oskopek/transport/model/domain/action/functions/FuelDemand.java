package com.oskopek.transport.model.domain.action.functions;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.FuelRoad;

/**
 * The fuel-demand function. Returns the the fuel cost of the {@link FuelRoad}.
 */
public class FuelDemand extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !FuelRoad.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("FuelDemand can only be applied to one argument of type FuelRoad");
        }
        return apply((FuelRoad) actionObjects[0]);
    }

    /**
     * Get the fuel cost of the {@link FuelRoad}.
     *
     * @param road the fuel road
     * @return the fuel cost
     */
    public ActionCost apply(FuelRoad road) {
        if (road == null) {
            throw new IllegalArgumentException("FuelRoad cannot be null.");
        }
        return road.getFuelCost();
    }

}
