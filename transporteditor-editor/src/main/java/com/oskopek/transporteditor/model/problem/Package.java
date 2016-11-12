package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Package extends DefaultLocatable implements ActionObject, Locatable {

    private final Location target;
    private final ActionCost size;

    public Package(String name, Location location, Location target, ActionCost size) {
        super(name, location);
        this.target = target;
        this.size = size;
    }

    public Location getTarget() {
        return target;
    }

    public ActionCost getSize() {
        return size;
    }

    public Package updateLocation(Location location) {
        return new Package(getName(), location, getTarget(), getSize());
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
