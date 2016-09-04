/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import javafx.beans.property.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Location implements Locatable {

    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty xCoordinate = new SimpleIntegerProperty();
    private final IntegerProperty yCoordinate = new SimpleIntegerProperty();
    private final ObjectProperty<Location> locationProperty = new ReadOnlyObjectWrapper<>(this);

    public Location(String name, Integer xCoordinate, Integer yCoordinate) {
        this.name.setValue(name);
        this.xCoordinate.setValue(xCoordinate);
        this.yCoordinate.setValue(yCoordinate);
    }

    @Override
    public Location getLocation() {
        return locationProperty.get();
    }

    @Override
    public void setLocation(Location location) {
        throw new UnsupportedOperationException("Cannot set location of a location.");
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getxCoordinate() {
        return xCoordinate.get();
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate.set(xCoordinate);
    }

    public IntegerProperty xCoordinateProperty() {
        return xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate.get();
    }

    public void setyCoordinate(int yCoordinate) {
        this.yCoordinate.set(yCoordinate);
    }

    public IntegerProperty yCoordinateProperty() {
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
        return "Loc[" + name + "]";
    }
}
