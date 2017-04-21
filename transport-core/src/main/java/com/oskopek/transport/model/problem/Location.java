package com.oskopek.transport.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents data of a node in the road graph. The X and Y coordinates do not necessarily map to the X and Y
 * coordinates in a rendered graph.
 */
public class Location extends DefaultActionObject implements Locatable {

    private final Integer xCoordinate;
    private final Integer yCoordinate;
    private final Boolean petrolStation;

    /**
     * Default constructor for [0, 0] positioned locations and a disabled petrol station.
     *
     * @param name the name
     */
    public Location(String name) {
        this(name, 0, 0);
    }

    /**
     * Default constructor for locations with a disabled petrol station.
     *
     * @param name the name
     * @param xCoordinate the X coordinate
     * @param yCoordinate the Y coordinate
     */
    public Location(String name, Integer xCoordinate, Integer yCoordinate) {
        this(name, xCoordinate, yCoordinate, null);
    }

    /**
     * Default constructor.
     *
     * @param name the name
     * @param xCoordinate the X coordinate
     * @param yCoordinate the Y coordinate
     * @param petrolStation true if has a petrol station, false if it doesn't in a fuel enabled domain
     * and null if it doesn't in a fuel disabled domain
     */
    public Location(String name, Integer xCoordinate, Integer yCoordinate, Boolean petrolStation) {
        super(name);
        this.xCoordinate = xCoordinate;
        if (xCoordinate == null) {
            throw new IllegalArgumentException("xCoordinate cannot be null.");
        }
        this.yCoordinate = yCoordinate;
        if (yCoordinate == null) {
            throw new IllegalArgumentException("xCoordinate cannot be null.");
        }
        this.petrolStation = petrolStation;
    }

    @Override
    public Location updateName(String newName) {
        return new Location(newName, getxCoordinate(), getyCoordinate(), petrolStation);
    }

    /**
     * Update the location's petrol station.
     *
     * @param hasPetrolStation the new value
     * @return a new location with the updated value
     */
    public Location updateHasPetrolStation(boolean hasPetrolStation) {
        return new Location(getName(), getxCoordinate(), getyCoordinate(), hasPetrolStation);
    }

    @Override
    public Location getLocation() {
        return this;
    }

    /**
     * Get the X coordinate.
     *
     * @return the X coordinate
     */
    public int getxCoordinate() {
        return xCoordinate;
    }

    /**
     * Get the Y coordinate.
     *
     * @return the Y coordinate
     */
    public int getyCoordinate() {
        return yCoordinate;
    }

    /**
     * Logical method for determining if the location has a petrol station.
     *
     * @return true iff it has a petrol station in a fuel enabled domain
     */
    public boolean hasPetrolStation() {
        return petrolStation != null && petrolStation;
    }

    /**
     * Get the petrol station field's value.
     *
     * @return true if has a petrol station, false if it doesn't in a fuel enabled domain
     * and null if it doesn't in a fuel disabled domain
     */
    public Boolean getPetrolStation() {
        return petrolStation;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getName()).toHashCode();
//                .append(petrolStation)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        Location location = (Location) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).isEquals();
//                .append(petrolStation, location.petrolStation)
    }

    @Override
    public String toString() {
        return "Loc[" + getName() + ']';
    }
}
