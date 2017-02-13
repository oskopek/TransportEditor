package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.builder.*;

import java.util.function.Consumer;

public abstract class ActionObjectBuilderConsumer<T> {

    public T create(Location location, Consumer<Location> updateCallback) {
        LocationBuilder builder = new LocationBuilder();
        builder.from(location, updateCallback);
        return createInternal(builder);
    }

    public T create(RoadGraph.RoadEdge roadEdge, Consumer<Road> updateCallback) {
        Road road = roadEdge.getRoad();
        if (road instanceof FuelRoad) {
            FuelRoad fuelRoad = (FuelRoad) road;
            FuelRoadBuilder builder = new FuelRoadBuilder();
            builder.from(fuelRoad, updateCallback);
            return createInternal(builder);
        } else {
            DefaultRoadBuilder<DefaultRoad> builder = new DefaultRoadBuilder<>();
            builder.from((DefaultRoad) road, updateCallback);
            return createInternal(builder);
        }
    }

    public T create(Vehicle vehicle, Consumer<Vehicle> updateCallback) {
        VehicleBuilder builder = new VehicleBuilder();
        builder.from(vehicle, updateCallback);
        return createInternal(builder);
    }

    public T create(Package pkg, Consumer<Package> updateCallback) {
        PackageBuilder builder = new PackageBuilder();
        builder.from(pkg, updateCallback);
        return createInternal(builder);
    }

    public T tryCreateFromLocatable(Problem problem, String name, Consumer<Problem> updateCallback) {
        Locatable locatable = problem.getLocatable(name);
        if (locatable instanceof Package) {
            Package oldPackage = (Package) locatable;
            return create(oldPackage, newPackage -> updateCallback.accept(problem.changePackage(oldPackage,
                    newPackage)));
        } else if (locatable instanceof Vehicle) {
            Vehicle oldVehicle = (Vehicle) locatable;
            return create(oldVehicle, newVehicle -> updateCallback.accept(problem.changeVehicle(oldVehicle,
                    newVehicle)));
        } else {
            return null;
        }
    }

    protected abstract T createInternal(ActionObjectBuilder<?> builder);

}
