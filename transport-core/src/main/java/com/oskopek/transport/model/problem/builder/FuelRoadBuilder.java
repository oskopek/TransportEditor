package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.FuelRoad;

/**
 * Action object builder for {@link FuelRoad}s.
 */
public class FuelRoadBuilder extends DefaultRoadBuilder<FuelRoad> {

    private ActionCost fuelCost;

    /**
     * Default constructor.
     */
    public FuelRoadBuilder() {
        // intentionally empty
    }

    /**
     * Get the fuel cost.
     *
     * @return the fuel cost
     */
    @FieldLocalization(key = "road.fuelcost")
    public ActionCost getFuelCost() {
        return fuelCost;
    }

    /**
     * Set the fuel cost.
     *
     * @param fuelCost the fuel cost to set
     */
    public void setFuelCost(ActionCost fuelCost) {
        this.fuelCost = fuelCost;
    }

    @Override
    public FuelRoad build() {
        return new FuelRoad(getName(), getLength(), fuelCost);
    }

    @Override
    public void from(FuelRoad instance) {
        super.from(instance);
        this.fuelCost = instance.getFuelCost();
    }
}
