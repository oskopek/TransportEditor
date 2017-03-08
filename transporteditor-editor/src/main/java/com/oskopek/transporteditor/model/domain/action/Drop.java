package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

/**
 * The drop action. Semantics: Vehicle (who) drops package (what) at location (where).
 */
public class Drop extends DefaultAction<Vehicle, Package> {

    /**
     * Default constructor.
     *
     * @param vehicle the vehicle
     * @param location where to drop
     * @param aPackage the package to drop
     * @param preconditions applicable preconditions
     * @param effects applicable effects
     * @param cost cost of the action
     * @param duration duration of the action
     */
    public Drop(Vehicle vehicle, Location location, Package aPackage, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("drop", vehicle, location, aPackage, preconditions, effects, cost, duration);
    }

    @Override
    public Problem applyPreconditions(Domain domain, Problem problemState) {
        String vehicleName = this.getWho().getName();
        String packageName = this.getWhat().getName();
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        Package pkg = problemState.getPackage(packageName);
        return problemState.putVehicle(vehicleName, vehicle.removePackage(pkg).updateReadyLoading(false))
                .putPackage(packageName, pkg.updateLocation(null));
    }

    @Override
    public Problem applyEffects(Domain domain, Problem problemState) {
        String vehicleName = this.getWho().getName();
        String packageName = this.getWhat().getName();
        String locationName = this.getWhere().getName();
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        Package pkg = problemState.getPackage(packageName);
        Location location = problemState.getRoadGraph().getLocation(locationName);
        Package newPackage = pkg.updateLocation(location);
        return problemState.putVehicle(vehicleName, vehicle.updateReadyLoading(true))
                .putPackage(packageName, newPackage);
    }
}
