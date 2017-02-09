package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.builder.*;

public abstract class ActionObjectBuilderConsumer<T> {

    public T create(Location location) {
        LocationBuilder builder = new LocationBuilder();
        builder.from(location);
        return createInternal(builder);
    }

    public T create(RoadGraph.RoadEdge roadEdge) {
        Road road = roadEdge.getRoad();
        if (road instanceof FuelRoad) {
            FuelRoad fuelRoad = (FuelRoad) road;
            FuelRoadBuilder builder = new FuelRoadBuilder();
            builder.from(fuelRoad);
            return createInternal(builder);
        } else {
            DefaultRoadBuilder<DefaultRoad> builder = new DefaultRoadBuilder<>();
            builder.from((DefaultRoad) road);
            return createInternal(builder);
        }
    }

    public T create(Vehicle vehicle) {
        VehicleBuilder builder = new VehicleBuilder();
        builder.from(vehicle);
        return createInternal(builder);
    }

    public T create(Package pkg) {
        PackageBuilder builder = new PackageBuilder();
        builder.from(pkg);
        return createInternal(builder);
    }

    public T tryCreateFromLocatable(Problem problem, String name) {
        Locatable locatable = problem.getLocatable(name);
        if (locatable instanceof Package) {
            return create((Package) locatable);
        } else if (locatable instanceof Vehicle) {
            return create((Vehicle) locatable);
        } else {
            return null;
        }
    }

    protected abstract T createInternal(ActionObjectBuilder<?> builder);

}
