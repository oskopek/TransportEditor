package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.DefaultActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;

public class LocatableBuilder<T extends DefaultActionObject & Locatable> extends DefaultActionObjectBuilder<T> {

    private Location location;

    public LocatableBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "location", priority = 1)
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public void from(T instance) {
        super.from(instance);
        setLocation(instance.getLocation());
    }
}
