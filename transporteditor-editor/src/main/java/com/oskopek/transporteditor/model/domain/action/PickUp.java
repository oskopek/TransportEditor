package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class PickUp extends DefaultAction<Vehicle, Package> {

    public PickUp(Vehicle vehicle, Location location, Package aPackage, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("pick-up", vehicle, location, aPackage, preconditions, effects, cost, duration);
    }

    @Override
    public Problem apply(Domain domain, Problem problemState) {
        String vehicleName = this.getWho().getName();
        String packageName = this.getWhat().getName();
        Vehicle vehicle = problemState.getVehicle(vehicleName);
        Package pkg = problemState.getPackage(packageName);
        Package newPackage = pkg.updateLocation(null);
        return problemState.updateVehicle(vehicleName, vehicle.addPackage(newPackage))
                .updatePackage(packageName, newPackage);
    }
}
