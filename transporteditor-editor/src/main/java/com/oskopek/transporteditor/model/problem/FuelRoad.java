package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FuelRoad extends DefaultRoad {

    private final ActionCost fuelCost;

    public FuelRoad(String name, ActionCost length) {
        this(name, length, length);
    }

    public FuelRoad(String name, ActionCost length, ActionCost fuelCost) {
        super(name, length);
        this.fuelCost = fuelCost;
    }

    @Override
    public FuelRoad updateName(String newName) {
        return new FuelRoad(newName, getLength(), getFuelCost());
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
        return fuelCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FuelRoad)) {
            return false;
        }
        FuelRoad fuelRoad = (FuelRoad) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getFuelCost(), fuelRoad.getFuelCost())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getFuelCost())
                .toHashCode();
    }
}
