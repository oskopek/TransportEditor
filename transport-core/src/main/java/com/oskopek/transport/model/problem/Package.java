package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a package in the Transport domain's problem instance.
 */
public class Package extends DefaultLocatable implements ActionObject, Locatable {

    private final Location target;
    private final ActionCost size;

    /**
     * Default constructor.
     *
     * @param name the name
     * @param location the current location
     * @param target the target location
     * @param size the size of the package
     */
    public Package(String name, Location location, Location target, ActionCost size) {
        super(name, location);
        this.target = target;
        this.size = size;
    }

    /**
     * Get the target location.
     *
     * @return the target location
     */
    public Location getTarget() {
        return target;
    }

    /**
     * Get the package size.
     *
     * @return the package size
     */
    public ActionCost getSize() {
        return size;
    }

    /**
     * Update the current location of the package. Returns a new package instance.
     *
     * @param location the new location
     * @return the updated package
     */
    public Package updateLocation(Location location) {
        return new Package(getName(), location, target, size);
    }

    /**
     * Update the target location of the package. Returns a new package instance.
     *
     * @param target the new target location
     * @return the updated package
     */
    public Package updateTarget(Location target) {
        return new Package(getName(), getLocation(), target, size);
    }

    @Override
    public Package updateName(String newName) {
        return new Package(newName, getLocation(), target, size);
    }

    /**
     * Update the package size. Returns a new package instance.
     *
     * @param size the new size
     * @return the updated package
     */
    public Package updateSize(ActionCost size) {
        return new Package(getName(), getLocation(), target, size);
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

        return new EqualsBuilder().appendSuper(super.equals(o)).append(target, aPackage.target)
                .append(size, aPackage.size).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(target).append(size)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Package[" + getName() + ", at=" + getLocation() + ", target=" + target + ", size=" + size
                + ']';
    }
}
