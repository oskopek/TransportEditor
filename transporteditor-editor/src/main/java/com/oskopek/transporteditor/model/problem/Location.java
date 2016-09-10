/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Location implements Locatable {

    private final String name;
    private final Integer xCoordinate;
    private final Integer yCoordinate;

    public Location(String name, Integer xCoordinate, Integer yCoordinate) {
        this.name = name;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    @Override
    public Location getLocation() {
        return this;
    }

    public String getName() {
        return name;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
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

        return new EqualsBuilder().append(getName(), location.getName()).append(getxCoordinate(),
                location.getxCoordinate()).append(getyCoordinate(), location.getyCoordinate()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).append(getxCoordinate()).append(getyCoordinate())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Loc[" + getName() + "]";
    }
}