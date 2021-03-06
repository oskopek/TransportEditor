package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Action object builder for {@link Vehicle}s.
 */
public class VehicleBuilder extends LocatableBuilder<Vehicle> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Location target;
    private ActionCost curCapacity;
    private ActionCost maxCapacity;
    private ActionCost curFuelCapacity;
    private ActionCost maxFuelCapacity;
    private boolean readyLoading;
    private List<Package> packageList;

    /**
     * Default constructor.
     */
    public VehicleBuilder() {
        // intentionally empty
    }

    /**
     * Get the target location.
     *
     * @return the target location
     */
    @FieldLocalization(key = "location.target", priority = 2)
    public Location getTarget() {
        return target;
    }

    /**
     * Set the target location.
     *
     * @param target the target location
     */
    public void setTarget(Location target) {
        this.target = target;
    }

    /**
     * Get the current capacity.
     *
     * @return the current capacity
     */
    @FieldLocalization(key = "vehicle.curcapacity", priority = 3)
    public ActionCost getCurCapacity() {
        return curCapacity;
    }

    /**
     * Set the current capacity.
     *
     * @param curCapacity the current capacity to set
     */
    public void setCurCapacity(ActionCost curCapacity) {
        this.curCapacity = curCapacity;
    }

    /**
     * Get the maximum capacity.
     *
     * @return the maximum capacity
     */
    @FieldLocalization(key = "vehicle.maxcapacity", priority = 3)
    public ActionCost getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Set the maximum capacity.
     *
     * @param maxCapacity the maximum capacity to set
     */
    public void setMaxCapacity(ActionCost maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Get the current fuel capacity.
     *
     * @return the current fuel capacity
     */
    @FieldLocalization(key = "vehicle.curfuelcapacity", priority = 4)
    public ActionCost getCurFuelCapacity() {
        return curFuelCapacity;
    }

    /**
     * Set the current fuel capacity.
     *
     * @param curFuelCapacity the current fuel capacity to set
     */
    public void setCurFuelCapacity(ActionCost curFuelCapacity) {
        this.curFuelCapacity = curFuelCapacity;
    }

    /**
     * Get the maximum fuel capacity.
     *
     * @return the maximum fuel capacity
     */
    @FieldLocalization(key = "vehicle.maxfuelcapacity", priority = 4)
    public ActionCost getMaxFuelCapacity() {
        return maxFuelCapacity;
    }

    /**
     * Set the maximum fuel capacity.
     *
     * @param maxFuelCapacity the maximum fuel capacity to set
     */
    public void setMaxFuelCapacity(ActionCost maxFuelCapacity) {
        this.maxFuelCapacity = maxFuelCapacity;
    }

    /**
     * Is the vehicle ready to be loaded.
     *
     * @return the ready-loading state
     */
    @FieldLocalization(key = "vehicle.readyloading", editable = false)
    public boolean isReadyLoading() {
        return readyLoading;
    }

    /**
     * Set the ready-loading state.
     *
     * @param readyLoading the ready-loading state to set
     */
    public void setReadyLoading(boolean readyLoading) {
        this.readyLoading = readyLoading;
    }

    /**
     * Get the package list.
     *
     * @return the package list
     */
    @FieldLocalization(key = "vehicle.packagelist", editable = false, priority = 6)
    public List<Package> getPackageList() {
        return packageList;
    }

    /**
     * Set the package list.
     *
     * @param packageList the package list to set
     */
    public void setPackageList(List<Package> packageList) {
        this.packageList = packageList;
    }

    @Override
    public Vehicle build() throws InvalidValueException {
        if (curCapacity.compareTo(maxCapacity) > 0) {
            throw new InvalidValueException("max. capacity > capacity");
        }
        if (curFuelCapacity.compareTo(maxFuelCapacity) > 0) {
            throw new InvalidValueException("max. fuel capacity > fuel capacity");
        }
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity, maxFuelCapacity,
                true, packageList);
    }

    @Override
    public void from(Vehicle instance) {
        super.from(instance);
        this.target = instance.getTarget();
        this.curCapacity = instance.getCurCapacity();
        this.maxCapacity = instance.getMaxCapacity();
        this.curFuelCapacity = instance.getCurFuelCapacity();
        this.maxFuelCapacity = instance.getMaxFuelCapacity();
        this.readyLoading = instance.isReadyLoading();
        this.packageList = instance.getPackageList();
    }
}
