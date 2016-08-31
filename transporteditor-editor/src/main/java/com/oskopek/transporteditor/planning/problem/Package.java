/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Package extends DefaultLocatable implements ActionObject, Locatable {

    private ObjectProperty<Location> target = new SimpleObjectProperty<>();
    private ObjectProperty<ActionCost> size = new SimpleObjectProperty<>();

    public Package(String name, Location location, Location target, ActionCost size) {
        super(name, location);
        this.target.setValue(target);
        this.size.setValue(size);
    }

    public Location getTarget() {
        return target.get();
    }

    public void setTarget(Location target) {
        this.target.set(target);
    }

    public ObjectProperty<Location> targetProperty() {
        return target;
    }

    public ActionCost getSize() {
        return size.get();
    }

    public void setSize(ActionCost size) {
        this.size.set(size);
    }

    public ObjectProperty<ActionCost> sizeProperty() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Package)) {
            return false;
        }

        Package aPackage = (Package) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(getTarget(), aPackage.getTarget()).append(
                getSize(), aPackage.getSize()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getTarget()).append(getSize())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Package[" + getName() + ", at=" + getLocation() + ", target=" + getTarget() + ", size=" + getSize()
                + "]";
    }
}
