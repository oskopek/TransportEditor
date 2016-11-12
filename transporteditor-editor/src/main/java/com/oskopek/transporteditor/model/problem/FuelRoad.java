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

    public static FuelRoad build(Road road, ActionCost fuelCost) {
        return new FuelRoad(road.getName(), road.getLength(), fuelCost);
    }

    public static FuelRoad build(Location from, Location to) {
        return build(from, to, null);
    }

    public static FuelRoad build(Location from, Location to, ActionCost length) {
        return build(from, to, length, null);
    }

    public static FuelRoad build(Location from, Location to, ActionCost length, ActionCost fuelCost) {
        return new FuelRoad(from + "->" + to, length, fuelCost);
    }

    public ActionCost getFuelCost() {
        return fuelCost.get();
    }
}
