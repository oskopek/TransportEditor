package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.*;

import java.util.List;

/**
 * The drive action. Semantics: Vehicle (who) drives from location (where) to location (what). Do note, that it only
 * supports driving along a single edge, not multiple edges (a path).
 */
public class Drive extends DefaultAction<Vehicle, Location> {

    /**
     * Default constructor.
     *
     * @param vehicle the vehicle
     * @param from from where to drive
     * @param to destination to drive to
     * @param preconditions applicable preconditions
     * @param effects applicable effects
     * @param road the road to use
     */
    public Drive(Vehicle vehicle, Location from, Location to, List<Predicate> preconditions,
            List<Predicate> effects, Road road) {
        super("drive", vehicle, from, to, preconditions, effects, road.getLength(), road.getLength());
    }

    @Override
    public Problem applyPreconditions(Domain domain, Problem problemState) {
        String name = this.getWho().getName();
        Location source = problemState.getRoadGraph().getLocation(getWhere().getName());
        Location destination = problemState.getRoadGraph().getLocation(getWhat().getName());
        Vehicle vehicle = problemState.getVehicle(name);
        ActionCost curFuelCapacity = vehicle.getCurFuelCapacity();
        Road road = problemState.getRoadGraph().getShortestRoadBetween(source, destination);
        if (domain.getPddlLabels().contains(PddlLabel.Fuel)) {
            FuelRoad fuelRoad = (FuelRoad) road;
            curFuelCapacity = curFuelCapacity.subtract(fuelRoad.getFuelCost());
        }
        return problemState
                .putVehicle(name, vehicle.updateCurFuelCapacity(curFuelCapacity).updateLocation(road.getLocation()));
    }

    @Override
    public Problem applyEffects(Domain domain, Problem problemState) {
        String name = this.getWho().getName();
        Location destination = problemState.getRoadGraph().getLocation(getWhat().getName());
        Vehicle vehicle = problemState.getVehicle(name);
        return problemState
                .putVehicle(name, vehicle.updateLocation(destination));
    }
}
