package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DefaultRoad extends DefaultActionObject implements Road {

    private final ActionCost length;

    public DefaultRoad(String name, ActionCost length) {
        super(name);
        this.length = length;
    }

    public static DefaultRoad build(Location from, Location to) {
        return build(from, to, null);
    }

    public static DefaultRoad build(Location from, Location to, ActionCost length) {
        return new DefaultRoad(from.getName() + "->" + to.getName(), length);
    }

    @Override
    public DefaultRoad updateLength(ActionCost length) {
        return new DefaultRoad(getName(), length);
    }

    @Override
    public DefaultRoad updateName(String newName) {
        return new DefaultRoad(newName, getLength());
    }

    @Override
    public ActionCost getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getLength())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DefaultRoad)) {
            return false;
        }

        DefaultRoad that = (DefaultRoad) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getLength(), that.getLength())
                .isEquals();
    }

    @Override
    public String toString() {
        return "Road[" + getName() + ": " + getLength() + ']';
    }
}
