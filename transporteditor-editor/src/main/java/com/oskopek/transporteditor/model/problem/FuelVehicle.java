/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;

import java.util.List;

public class FuelVehicle extends Vehicle {

    private final ActionCost curFuelCapacity;
    private final ActionCost maxFuelCapacity;

    public FuelVehicle(String name, Location location, ActionCost curCapacity, ActionCost maxCapacity,
            List<Package> packageList, ActionCost curFuelCapacity, ActionCost maxFuelCapacity) {
        super(name, location, curCapacity, maxCapacity, packageList);
        this.curFuelCapacity = curFuelCapacity;
        this.maxFuelCapacity = maxFuelCapacity;
    }

    public ActionCost getCurFuelCapacity() {
        return curFuelCapacity;
    }

    public ActionCost getMaxFuelCapacity() {
        return maxFuelCapacity;
    }

    public FuelVehicle updateCurFuelCapacity(ActionCost curFuelCapacity) {
        return new FuelVehicle(getName(), getLocation(), getCurCapacity(), getMaxCapacity(), getPackageList(),
                curFuelCapacity,
                getMaxFuelCapacity());
    }
}
