/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
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
}
