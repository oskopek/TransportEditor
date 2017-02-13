package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Location extends DefaultActionObject implements Locatable {

    private final Integer xCoordinate;
    private final Integer yCoordinate;
    private final Boolean petrolStation;

    public Location(String name) {
        this(name, 0, 0);
    }

    public Location(String name, Integer xCoordinate, Integer yCoordinate) {
        this(name, xCoordinate, yCoordinate, null);
    }

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
        return new Location(newName, getxCoordinate(), getyCoordinate(), getPetrolStation());
    }

    public Location updateHasPetrolStation(boolean hasPetrolStation) {
        return new Location(getName(), getxCoordinate(), getyCoordinate(), hasPetrolStation);
    }

    @Override
    public Location getLocation() {
        return this;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public boolean hasPetrolStation() {
        return petrolStation != null && petrolStation;
    }

    public Boolean getPetrolStation() {
        return petrolStation;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getName())
                .append(getxCoordinate()).append(getyCoordinate()).append(getPetrolStation()).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(o)).append(getxCoordinate(), location.getxCoordinate())
                .append(getyCoordinate(), location.getyCoordinate())
                .append(getPetrolStation(), location.getPetrolStation()).isEquals();
    }

    @Override
    public String toString() {
        return "Loc[" + getName() + "]";
    }
}
