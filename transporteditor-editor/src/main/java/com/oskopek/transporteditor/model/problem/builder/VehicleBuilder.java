package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class VehicleBuilder extends LocatableBuilder<Vehicle> {

    private ActionCost curCapacity;
    private ActionCost maxCapacity;
    private ActionCost curFuelCapacity;
    private ActionCost maxFuelCapacity;
    private List<Package> packageList;

    public VehicleBuilder() {
        // intentionally empty
    }

    // TODO: Set automatically maxFuelCapacity - packageSizeSum
    @FieldLocalization(key = "vehicle.curcapacity", priority = 3, editable = false)
    public ActionCost getCurCapacity() {
        return curCapacity;
    }

    public void setCurCapacity(ActionCost curCapacity) {
        this.curCapacity = curCapacity;
    }

    @FieldLocalization(key = "vehicle.maxcapacity", priority = 3)
    public ActionCost getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(ActionCost maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @FieldLocalization(key = "vehicle.curfuelcapacity", priority = 4) // TODO: Validate to be max maxFuelCapacity
    public ActionCost getCurFuelCapacity() {
        return curFuelCapacity;
    }

    public void setCurFuelCapacity(ActionCost curFuelCapacity) {
        this.curFuelCapacity = curFuelCapacity;
    }

    @FieldLocalization(key = "vehicle.maxfuelcapacity", priority = 4)
    public ActionCost getMaxFuelCapacity() {
        return maxFuelCapacity;
    }

    public void setMaxFuelCapacity(ActionCost maxFuelCapacity) {
        this.maxFuelCapacity = maxFuelCapacity;
    }

    @FieldLocalization(key = "vehicle.packagelist", editable = false)
    public List<Package> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<Package> packageList) {
        this.packageList = packageList;
    }

    @Override
    public Vehicle build() {
        return new Vehicle(getName(), getLocation(), getCurCapacity(), getMaxCapacity(), getCurFuelCapacity(),
                getMaxFuelCapacity(), getPackageList());
    }

    @Override
    public void from(Vehicle instance) {
        super.from(instance);
        setCurCapacity(instance.getCurCapacity());
        setMaxCapacity(instance.getMaxCapacity());
        setCurFuelCapacity(instance.getCurFuelCapacity());
        setMaxFuelCapacity(instance.getMaxFuelCapacity());
        setPackageList(instance.getPackageList());
    }
}
