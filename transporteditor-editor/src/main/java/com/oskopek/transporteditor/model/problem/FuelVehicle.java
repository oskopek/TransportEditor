/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;

public class FuelVehicle extends Vehicle {

    private final ObjectProperty<ActionCost> curFuelCapacity = new SimpleObjectProperty<>();
    private final ObjectProperty<ActionCost> maxFuelCapacity = new SimpleObjectProperty<>();

    public FuelVehicle(String name, Location location, ActionCost curCapacity, ActionCost maxCapacity,
            List<Package> packageList, ActionCost curFuelCapacity, ActionCost maxFuelCapacity) {
        super(name, location, curCapacity, maxCapacity, packageList);
        this.curFuelCapacity.setValue(curFuelCapacity);
        this.maxFuelCapacity.setValue(maxFuelCapacity);
    }

    public ActionCost getCurFuelCapacity() {
        return curFuelCapacity.get();
    }

    public void setCurFuelCapacity(ActionCost curFuelCapacity) {
        this.curFuelCapacity.set(curFuelCapacity);
    }

    public ObjectProperty<ActionCost> curFuelCapacityProperty() {
        return curFuelCapacity;
    }

    public ActionCost getMaxFuelCapacity() {
        return maxFuelCapacity.get();
    }

    public void setMaxFuelCapacity(ActionCost maxFuelCapacity) {
        this.maxFuelCapacity.set(maxFuelCapacity);
    }

    public ObjectProperty<ActionCost> maxFuelCapacityProperty() {
        return maxFuelCapacity;
    }

}
