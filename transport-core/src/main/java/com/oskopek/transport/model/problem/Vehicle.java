package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vehicle in the Transport domain's problem instance. Universal for all domain variants.
 */
public class Vehicle extends DefaultLocatable implements Locatable, ActionObject {

    private final Location target;
    private final ActionCost curCapacity;
    private final ActionCost maxCapacity;
    private final ActionCost curFuelCapacity;
    private final ActionCost maxFuelCapacity;
    private final List<Package> packageList;
    private final boolean readyLoading;

    /**
     * Default constructor for a fuel disabled domain.
     *
     * @param name the name
     * @param location the starting location of the vehicle
     * @param target the target location of the vehicle
     * @param curCapacity the current package capacity (the sum of package sizes that fit into the vehicle)
     * @param maxCapacity the maximum package capacity (the sum of package sizes that fit into the vehicle
     * if it is empty before)
     * @param readyLoading the ready-laoding state
     * @param packageList a list of packages loaded into the vehicle
     */
    public Vehicle(String name, Location location, Location target, ActionCost curCapacity, ActionCost maxCapacity,
            boolean readyLoading, List<Package> packageList) {
        this(name, location, target, curCapacity, maxCapacity, null, null, readyLoading, packageList);
    }

    /**
     * Default constructor for a fuel enabled domain.
     *
     * @param name the name
     * @param location the current location of the vehicle
     * @param target the target location of the vehicle
     * @param curCapacity the current package capacity (the sum of package sizes that fit into the vehicle)
     * @param maxCapacity the maximum package capacity (the sum of package sizes that fit into the vehicle
     * if it is empty before)
     * @param curFuelCapacity the current fuel capacity (the sum of road fuel costs that the vehicle can drive
     * from now)
     * @param maxFuelCapacity the maximum fuel capacity (the sum of road fuel costs that the vehicle can drive
     * from a full tank)
     * @param readyLoading the ready-laoding state
     * @param packageList a list of packages loaded into the vehicle
     */
    public Vehicle(String name, Location location, Location target, ActionCost curCapacity, ActionCost maxCapacity,
            ActionCost curFuelCapacity, ActionCost maxFuelCapacity, boolean readyLoading,
            List<Package> packageList) {
        super(name, location);
        if (packageList == null) {
            throw new IllegalArgumentException("Package list cannot be null.");
        }
        this.target = target;
        this.curCapacity = curCapacity;
        this.maxCapacity = maxCapacity;
        this.curFuelCapacity = curFuelCapacity;
        this.maxFuelCapacity = maxFuelCapacity;
        this.readyLoading = readyLoading;
        this.packageList = packageList;
    }

    /**
     * Get the target location.
     *
     * @return the target location, may be null
     */
    public Location getTarget() {
        return target;
    }

    /**
     * Get the current package capacity.
     *
     * @return the current package capacity
     */
    public ActionCost getCurCapacity() {
        return curCapacity;
    }

    /**
     * Get the maximum package capacity.
     *
     * @return the maximum package capacity
     */
    public ActionCost getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Get the current fuel capacity.
     *
     * @return the current fuel capacity
     */
    public ActionCost getCurFuelCapacity() {
        return curFuelCapacity;
    }

    /**
     * Get the maximum fuel capacity.
     *
     * @return the maximum fuel capacity
     */
    public ActionCost getMaxFuelCapacity() {
        return maxFuelCapacity;
    }

    /**
     * Get whether is in ready-loading state.
     *
     * @return the ready-loading state
     */
    public boolean isReadyLoading() {
        return readyLoading;
    }

    /**
     * Get the package list.
     *
     * @return the package list
     */
    public List<Package> getPackageList() {
        return packageList;
    }

    @Override
    public Vehicle updateName(String newName) {
        return new Vehicle(newName, getLocation(), target, curCapacity, maxCapacity, curFuelCapacity,
                maxFuelCapacity, readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new current fuel capacity. Returns a new vehicle instance.
     *
     * @param curFuelCapacity the new current fuel capacity
     * @return the updated vehicle
     */
    public Vehicle updateCurFuelCapacity(ActionCost curFuelCapacity) {
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity,
                maxFuelCapacity, readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new target location. Returns a new vehicle instance.
     *
     * @param target the new target location
     * @return the updated vehicle
     */
    public Vehicle updateTarget(Location target) {
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity,
                maxFuelCapacity, readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new location. Returns a new vehicle instance.
     *
     * @param location the new location
     * @return the updated vehicle
     */
    public Vehicle updateLocation(Location location) {
        return new Vehicle(getName(), location, target, curCapacity, maxCapacity, curFuelCapacity,
                maxFuelCapacity, readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new ready loading state. Returns a new vehicle instance.
     *
     * @param readyLoading the new ready-loading state
     * @return the updated vehicle
     */
    public Vehicle updateReadyLoading(boolean readyLoading) {
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity, maxFuelCapacity,
                readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new current capacity. Returns a new vehicle instance.
     *
     * @param curCapacity the new current capacity
     * @return the updated vehicle
     */
    public Vehicle updateCurCapacity(ActionCost curCapacity) {
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity,
                maxFuelCapacity, readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new maximum capacity. Returns a new vehicle instance.
     *
     * @param maxCapacity the new maximum capacity
     * @return the updated vehicle
     */
    public Vehicle updateMaxCapacity(ActionCost maxCapacity) {
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity,
                maxFuelCapacity, readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new maximum fuel capacity. Returns a new vehicle instance.
     *
     * @param maxFuelCapacity the new maximum fuel capacity
     * @return the updated vehicle
     */
    public Vehicle updateMaxFuelCapacity(ActionCost maxFuelCapacity) {
        return new Vehicle(getName(), getLocation(), target, curCapacity, maxCapacity, curFuelCapacity, maxFuelCapacity,
                readyLoading, packageList);
    }

    /**
     * Update the vehicle with a new package. Returns a new vehicle instance.
     *
     * @param oldPackage the old package
     * @param newPackage the new package
     * @return the updated vehicle
     */
    public Vehicle changePackage(Package oldPackage, Package newPackage) {
        return removePackage(oldPackage).addPackage(newPackage);
    }

    /**
     * Adds a package to the vehicle. Returns a new vehicle instance.
     *
     * @param pkg the package to add
     * @return the updated vehicle
     */
    public Vehicle addPackage(Package pkg) {
        if (packageList.contains(pkg)) {
            throw new IllegalArgumentException("Package " + pkg + " already is in vehicle " + this + '.');
        }
        if (pkg.getLocation() != null) {
            throw new IllegalStateException(
                    "Package " + pkg + " is in vehicle and somewhere else at the same time " + pkg.getLocation() + '.');
        }
        List<Package> newPackageList = new ArrayList<>(packageList);
        newPackageList.add(pkg);
        return new Vehicle(getName(), getLocation(), target, curCapacity.subtract(pkg.getSize()), maxCapacity,
                curFuelCapacity, maxFuelCapacity, readyLoading, newPackageList);
    }

    /**
     * Removes a package from the vehicle. Returns a new vehicle instance.
     *
     * @param pkg the package to remove
     * @return the updated vehicle
     */
    public Vehicle removePackage(Package pkg) {
        if (!packageList.contains(pkg)) {
            throw new IllegalArgumentException("Package " + pkg + " is not in vehicle " + this + '.');
        }
        if (pkg.getLocation() != null) {
            throw new IllegalStateException(
                    "Package " + pkg + " is in vehicle and somewhere else at the same time " + pkg.getLocation() + '.');
        }
        List<Package> newPackageList = new ArrayList<>(packageList);
        newPackageList.remove(pkg);
        return new Vehicle(getName(), getLocation(), target, curCapacity.add(pkg.getSize()), maxCapacity,
                curFuelCapacity, maxFuelCapacity, readyLoading, newPackageList);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode())
                .append(target).append(curCapacity).append(maxCapacity).append(curFuelCapacity).append(maxFuelCapacity)
                .append(readyLoading).append(packageList).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle)) {
            return false;
        }
        Vehicle vehicle = (Vehicle) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(target, vehicle.target)
                .append(curCapacity, vehicle.curCapacity)
                .append(maxCapacity, vehicle.maxCapacity)
                .append(curFuelCapacity, vehicle.curFuelCapacity)
                .append(maxFuelCapacity, vehicle.maxFuelCapacity)
                .append(readyLoading, vehicle.readyLoading)
                .append(packageList, vehicle.packageList)
                .isEquals();
    }

    @Override
    public String toString() {
        return "Vehicle[" + getName() + ", at=" + getLocation() + ", target=" + target + ", capacity="
                + curCapacity + '/' + maxCapacity + ", fuel=" + curFuelCapacity + '/'
                + maxFuelCapacity + ", readyLoading=" + readyLoading + ", packages=" + packageList + ']';
    }
}
