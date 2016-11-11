/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class Vehicle extends DefaultLocatable implements Locatable, ActionObject {

    private final ActionCost curCapacity;
    private final ActionCost maxCapacity;
    private final ActionCost curFuelCapacity;
    private final ActionCost maxFuelCapacity;
    private final List<Package> packageList;

    public Vehicle(String name, Location location, ActionCost curCapacity, ActionCost maxCapacity,
            List<Package> packageList) {
        this(name, location, curCapacity, maxCapacity, null, null, packageList);
    }

    public Vehicle(String name, Location location, ActionCost curCapacity, ActionCost maxCapacity,
            ActionCost curFuelCapacity, ActionCost maxFuelCapacity,
            List<Package> packageList) {
        super(name, location);
        this.curCapacity = curCapacity;
        this.maxCapacity = maxCapacity;
        this.curFuelCapacity = curFuelCapacity;
        this.maxFuelCapacity = maxFuelCapacity;
        this.packageList = packageList;
    }

    public ActionCost getCurCapacity() {
        return curCapacity;
    }

    public ActionCost getMaxCapacity() {
        return maxCapacity;
    }

    public ActionCost getCurFuelCapacity() {
        return curFuelCapacity;
    }

    public ActionCost getMaxFuelCapacity() {
        return maxFuelCapacity;
    }

    public List<Package> getPackageList() {
        return packageList;
    }

    public Vehicle updateCurFuelCapacity(ActionCost curFuelCapacity) {
        return new Vehicle(getName(), getLocation(), getCurCapacity(), getMaxCapacity(), curFuelCapacity,
                getMaxFuelCapacity(), getPackageList());
    }

    public Vehicle updateLocation(Location location) {
        return new Vehicle(getName(), location, getCurCapacity(), getMaxCapacity(), getCurFuelCapacity(),
                getMaxFuelCapacity(), getPackageList());
    }

    public Vehicle addPackage(Package pkg) {
        if (getPackageList().contains(pkg)) {
            throw new IllegalArgumentException("Package " + pkg + " already is in vehicle " + this + ".");
        }
        if (pkg.getLocation() != null) {
            throw new IllegalStateException(
                    "Package " + pkg + " is in vehicle and somewhere else at the same time " + pkg.getLocation() + ".");
        }
        List<Package> newPackageList = new ArrayList<>(getPackageList());
        newPackageList.add(pkg);
        return new Vehicle(getName(), getLocation(), getCurCapacity(), getMaxCapacity(), getCurFuelCapacity(),
                getMaxFuelCapacity(), newPackageList);
    }

    public Vehicle removePackage(Package pkg) {
        if (!getPackageList().contains(pkg)) {
            throw new IllegalArgumentException("Package " + pkg + " is not in vehicle " + this + ".");
        }
        if (pkg.getLocation() != null) {
            throw new IllegalStateException(
                    "Package " + pkg + " is in vehicle and somewhere else at the same time " + pkg.getLocation() + ".");
        }
        List<Package> newPackageList = new ArrayList<>(getPackageList());
        newPackageList.remove(pkg);
        return new Vehicle(getName(), getLocation(), getCurCapacity(), getMaxCapacity(), getCurFuelCapacity(),
                getMaxFuelCapacity(), newPackageList);
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
        return new EqualsBuilder().appendSuper(super.equals(o)).append(getCurCapacity(), vehicle.getCurCapacity())
                .append(getMaxCapacity(), vehicle.getMaxCapacity())
                .append(getCurFuelCapacity(), vehicle.getCurFuelCapacity())
                .append(getMaxFuelCapacity(), vehicle.getMaxFuelCapacity())
                .append(getPackageList(), vehicle.getPackageList())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getCurCapacity()).append(
                getMaxCapacity()).append(getCurFuelCapacity()).append(
                getMaxFuelCapacity()).append(getPackageList()).toHashCode();
    }

    @Override
    public String toString() {
        return "Vehicle[" + getName() + ", at=" + getLocation() + ", capacity=" + getCurCapacity() + "/"
                + getMaxCapacity() + ", fuel=" + getCurFuelCapacity() + "/" + getMaxFuelCapacity() + ", packages="
                + getPackageList() + "]";
    }
}
