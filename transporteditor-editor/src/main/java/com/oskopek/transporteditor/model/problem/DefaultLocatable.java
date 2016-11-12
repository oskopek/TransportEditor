package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DefaultLocatable extends DefaultActionObject implements Locatable, ActionObject {

    private final Location location;

    public DefaultLocatable(String name, Location location) {
        super(name);
        this.location = location;
    }

    @Override
    public Location getLocation() {
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
