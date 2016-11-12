package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.*;

import java.util.List;

public class Drive extends DefaultAction<Vehicle, Location> {

    private final ActionCost fuelCost;

    public Drive(Vehicle vehicle, Location location, Location location2, List<Predicate> preconditions,
            List<Predicate> effects, Road road) {
        super("drive", vehicle, location, location2, preconditions, effects, road.getLength(), road.getLength());

        if (road instanceof FuelRoad) {
            fuelCost = ((FuelRoad) road).getFuelCost();
        } else {
            fuelCost = null;
        }
    }

    @Override
    public Problem apply(Domain domain, Problem problemState) {
        String name = this.getWho().getName();
        Location destination = problemState.getRoadGraph().getLocation(getWhat().getName());
        Vehicle vehicle = problemState.getVehicle(name);
        ActionCost curFuelCapacity = vehicle.getCurFuelCapacity();
        if (domain.getPddlLabels().contains(PddlLabel.Fuel)) {
            curFuelCapacity = curFuelCapacity.subtract(this.fuelCost);
        }
        return problemState
                .updateVehicle(name, vehicle.updateCurFuelCapacity(curFuelCapacity).updateLocation(destination));
    }
}
