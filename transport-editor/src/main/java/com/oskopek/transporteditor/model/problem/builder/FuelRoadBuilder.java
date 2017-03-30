package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.FuelRoad;

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
        return new FuelRoad(getName(), getLength(), getFuelCost());
    }

    @Override
    public void from(FuelRoad instance) {
        super.from(instance);
        setFuelCost(instance.getFuelCost());
    }
}
