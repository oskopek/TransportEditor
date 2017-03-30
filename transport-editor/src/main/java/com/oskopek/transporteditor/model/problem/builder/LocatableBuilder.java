package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.DefaultActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;

/**
 * Action object builder for {@link Locatable}s.
 *
 * @param <T> the type of the action object
 */
public abstract class LocatableBuilder<T extends DefaultActionObject & Locatable>
        extends DefaultActionObjectBuilder<T> {

    private Location location;

    /**
     * Default constructor.
     */
    public LocatableBuilder() {
        // intentionally empty
    }

    /**
     * Get the location.
     *
     * @return the location
     */
    @FieldLocalization(key = "location", priority = 1)
    public Location getLocation() {
        return location;
    }

    /**
     * Set the location.
     *
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public void from(T instance) {
        super.from(instance);
        setLocation(instance.getLocation());
    }
}
