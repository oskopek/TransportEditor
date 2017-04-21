package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Default road implementation. Contains only the length and name of a road.
 */
public class DefaultRoad extends DefaultActionObject implements Road {

    private final ActionCost length;
    private final Location location;

    /**
     * Default constructor.
     *
     * @param name the name
     * @param length the length
     */
    public DefaultRoad(String name, ActionCost length) {
        super(name);
        this.length = length;
        this.location = new Location(name);
    }

    /**
     * Static builder method.
     *
     * @param from the source location
     * @param to the destination location
     * @return the road instance
     */
    public static DefaultRoad build(Location from, Location to) {
        return build(from, to, null);
    }

    /**
     * Static builder method.
     *
     * @param from the source location
     * @param to the destination location
     * @param length the length
     * @return the road instance
     */
    public static DefaultRoad build(Location from, Location to, ActionCost length) {
        return new DefaultRoad(from.getName() + "->" + to.getName(), length);
    }

    @Override
    public DefaultRoad updateLength(ActionCost length) {
        return new DefaultRoad(getName(), length);
    }

    @Override
    public DefaultRoad updateName(String newName) {
        return new DefaultRoad(newName, length);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public ActionCost getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(length)
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
                .append(length, that.length)
                .isEquals();
    }

    @Override
    public String toString() {
        return "Road[" + getName() + ": " + length + ']';
    }
}
