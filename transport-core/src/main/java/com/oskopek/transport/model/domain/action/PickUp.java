package com.oskopek.transport.model.domain.action;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;

import java.util.List;

/**
 * The pick-up action. Semantics: Vehicle (who) picks up package (what) at location (where).
 */
public class PickUp extends DefaultAction<Vehicle, Package> {

    /**
     * Default constructor.
     *
     * @param vehicle the vehicle
     * @param location where to pick up
     * @param aPackage the package to pick up
     * @param preconditions applicable preconditions
     * @param effects applicable effects
     * @param cost cost of the action
     * @param duration duration of the action
     */
    public PickUp(Vehicle vehicle, Location location, Package aPackage, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("pick-up", vehicle, location, aPackage, preconditions, effects, cost, duration);
    }

    @Override
    public Problem applyPreconditions(Domain domain, Problem problemState) {
        String vehicleName = this.getWho().getName();
        String packageName = this.getWhat().getName();
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        Package pkg = problemState.getPackage(packageName);
        return problemState.putVehicle(vehicleName, vehicle.updateReadyLoading(false))
                .putPackage(packageName, pkg.updateLocation(null));
    }

    @Override
    public Problem applyEffects(Domain domain, Problem problemState) {
        String vehicleName = this.getWho().getName();
        String packageName = this.getWhat().getName();
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        Package pkg = problemState.getPackage(packageName);
        Package newPackage = pkg.updateLocation(null);
        return problemState.putVehicle(vehicleName, vehicle.updateReadyLoading(true).addPackage(newPackage))
                .putPackage(packageName, newPackage);
    }
}
