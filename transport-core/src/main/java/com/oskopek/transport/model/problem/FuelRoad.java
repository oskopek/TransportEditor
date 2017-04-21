package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Adds the fuel cost on top of normal rods.
 */
public class FuelRoad extends DefaultRoad {

    private final ActionCost fuelCost;

    /**
     * Non-fuel constructor. The fuel cost will be set to the length.
     *
     * @param name the name
     * @param length the lenght (and fuel cost)
     */
    public FuelRoad(String name, ActionCost length) {
        this(name, length, length);
    }

    /**
     * Default constructor.
     *
     * @param name the name
     * @param length the length
     * @param fuelCost the fuel cost
     */
    public FuelRoad(String name, ActionCost length, ActionCost fuelCost) {
        super(name, length);
        this.fuelCost = fuelCost;
    }

    @Override
    public FuelRoad updateName(String newName) {
        return new FuelRoad(newName, getLength(), fuelCost);
    }

    @Override
    public FuelRoad updateLength(ActionCost length) {
        return new FuelRoad(getName(), length, fuelCost);
    }

    /**
     * Static road copy enhancer method. Adds fuel to a road and returns the new road instance.
     *
     * @param road the road
     * @param fuelCost the fuel cost
     * @return the fuel-enabled road
     */
    public static FuelRoad build(Road road, ActionCost fuelCost) {
        return new FuelRoad(road.getName(), road.getLength(), fuelCost);
    }

    /**
     * Static builder method.
     *
     * @param from the source location
     * @param to the destination location
     * @return the fuel-enabled road
     */
    public static FuelRoad build(Location from, Location to) {
        return build(from, to, null);
    }

    /**
     * Static builder method.
     *
     * @param from the source location
     * @param to the destination location
     * @param length the lenght
     * @return the fuel-enabled road
     */
    public static FuelRoad build(Location from, Location to, ActionCost length) {
        return build(from, to, length, null);
    }

    /**
     * Static builder method.
     *
     * @param from the source location
     * @param to the destination location
     * @param length the lenght
     * @param fuelCost the fuel cost
     * @return the fuel-enabled road
     */
    public static FuelRoad build(Location from, Location to, ActionCost length, ActionCost fuelCost) {
        return new FuelRoad(from.getName() + "->" + to.getName(), length, fuelCost);
    }

    /**
     * Get the fuel cost.
     *
     * @return the fuel cost
     */
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
                .append(fuelCost, fuelRoad.fuelCost)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(fuelCost)
                .toHashCode();
    }
}
