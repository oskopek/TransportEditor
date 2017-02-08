package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.FuelRoad;

public class FuelRoadBuilder extends DefaultRoadBuilder<FuelRoad> {

    private ActionCost fuelCost;

    public FuelRoadBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "road.fuelcost")
    public ActionCost getFuelCost() {
        return fuelCost;
    }

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
