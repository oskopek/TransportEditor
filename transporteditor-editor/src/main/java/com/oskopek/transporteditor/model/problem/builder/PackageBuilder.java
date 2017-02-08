package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;

public class PackageBuilder extends LocatableBuilder<Package> {

    private Location target;
    private ActionCost size;

    public PackageBuilder() {
        // intentionally empty
    }

    @FieldLocalization(key = "location.target", priority = 4)
    public Location getTarget() {
        return target;
    }

    public void setTarget(Location target) {
        this.target = target;
    }

    @FieldLocalization(key = "size")
    public ActionCost getSize() {
        return size;
    }

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
