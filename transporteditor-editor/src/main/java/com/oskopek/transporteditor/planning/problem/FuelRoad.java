/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FuelRoad extends DefaultRoad {

    private final ObjectProperty<ActionCost> fuelCost = new SimpleObjectProperty<>();

    public FuelRoad(String name, ActionCost length) {
        this(name, length, length);
    }

    public FuelRoad(String name, ActionCost length, ActionCost fuelCost) {
        super(name, length);
        this.fuelCost.setValue(fuelCost);
    }

    public ActionCost getFuelCost() {
        return fuelCost.get();
    }

    public void setFuelCost(ActionCost fuelCost) {
        this.fuelCost.set(fuelCost);
    }

    public ObjectProperty<ActionCost> fuelCostProperty() {
        return fuelCost;
    }
}
