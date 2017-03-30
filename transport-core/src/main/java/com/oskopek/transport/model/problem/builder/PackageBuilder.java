package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;

/**
 * Action object builder for {@link Package}s.
 */
public class PackageBuilder extends LocatableBuilder<Package> {

    private Location target;
    private ActionCost size;

    /**
     * Default constructor.
     */
    public PackageBuilder() {
        // intentionally empty
    }

    /**
     * Get the target.
     *
     * @return the target
     */
    @FieldLocalization(key = "location.target", priority = 4)
    public Location getTarget() {
        return target;
    }

    /**
     * Set the target.
     *
     * @param target the target to set
     */
    public void setTarget(Location target) {
        this.target = target;
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    @FieldLocalization(key = "size")
    public ActionCost getSize() {
        return size;
    }

    /**
     * Set the size.
     *
     * @param size the size to set
     */
    public void setSize(ActionCost size) {
        this.size = size;
    }

    @Override
    public Package build() {
        return new Package(getName(), getLocation(), getTarget(), getSize());
    }

    @Override
    public void from(Package instance) {
        super.from(instance);
        setTarget(instance.getTarget());
        setSize(instance.getSize());
    }
}
