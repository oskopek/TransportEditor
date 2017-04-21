package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;

public class HasFuelCapacityForDrive extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        if (action instanceof Drive) {
            Drive drive = (Drive) action;
            Road road = drive.getRoad();
            if (drive.isFuelDomain()) {
                Vehicle vehicle = state.getVehicle(action.getWho().getName());
                FuelRoad fuelRoad = (FuelRoad) road;
                return vehicle.getCurFuelCapacity().subtract(fuelRoad.getFuelCost()).getCost() >= 0;
            }
        }
        return true;
    }
}
