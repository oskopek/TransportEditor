package com.oskopek.transport.model.domain.action;

import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.*;

import java.util.List;

/**
 * The drive action. Semantics: Vehicle (who) drives from location (where) to location (what). Do note, that it only
 * supports driving along a single edge, not multiple edges (a path).
 */
public class Drive extends DefaultAction<Vehicle, Location> {

    private final Road road;
    private final Location dest;
    private final String vehicleName;
    private final boolean isFuelDomain;

    /**
     * Default constructor.
     *
     * @param vehicle the vehicle
     * @param from from where to drive
     * @param to destination to drive to
     * @param preconditions applicable preconditions
     * @param effects applicable effects
     * @param road the road to use
     * @param isFuelDomain is the domain a fuel-enabled one
     */
    public Drive(Vehicle vehicle, Location from, Location to, List<Predicate> preconditions,
            List<Predicate> effects, Road road, boolean isFuelDomain) {
        super("drive", vehicle, from, to, preconditions, effects, road.getLength(), road.getLength());
        this.road = road;
        this.dest = to;
        this.vehicleName = vehicle != null ? vehicle.getName() : null;
        this.isFuelDomain = isFuelDomain;
    }

    @Override
    public Problem applyPreconditions(Problem problemState) {
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        ActionCost curFuelCapacity = vehicle.getCurFuelCapacity();
        if (isFuelDomain) {
            FuelRoad fuelRoad = (FuelRoad) road;
            curFuelCapacity = curFuelCapacity.subtract(fuelRoad.getFuelCost());
        }
        return problemState.putVehicle(vehicleName, vehicle.updateCurFuelCapacity(curFuelCapacity)
                .updateLocation(road.getLocation()));
    }

    @Override
    public Problem applyEffects(Problem problemState) {
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        return problemState
                .putVehicle(vehicleName, vehicle.updateLocation(dest));
    }
}
