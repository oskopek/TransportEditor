package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Implementation of a locatable - an action object with an associated location.
 */
public class DefaultLocatable extends DefaultActionObject implements Locatable, ActionObject {

    private final Location location;

    /**
     * Default constructor.
     *
     * @param name the name
     * @param location the location
     */
    public DefaultLocatable(String name, Location location) {
        super(name);
        this.location = location;
    }

    @Override
    public DefaultLocatable updateName(String newName) {
        return new DefaultLocatable(newName, getLocation());
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getLocation()).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(o)).append(getLocation(), that.getLocation()).isEquals();
    }
}
