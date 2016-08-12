/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DefaultLocatable extends DefaultActionObject implements Locatable, ActionObject {

    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();

    public DefaultLocatable(String name, Location location) {
        super(name);
        this.location.setValue(location);
    }

    @Override
    public Location getLocation() {
        return location.get();
    }

    @Override
    public void setLocation(Location location) {
        this.location.set(location);
    }

    @Override
    public ObjectProperty<Location> locationProperty() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultLocatable)) {
            return false;
        }
        DefaultLocatable that = (DefaultLocatable) o;
        return new EqualsBuilder().append(getLocation(), that.getLocation()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getLocation()).toHashCode();
    }
}
