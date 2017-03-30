package com.oskopek.transporteditor.view;

import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.builder.*;

import java.util.function.Consumer;

/**
 * Abstract class that serves as a backend for creating popups and edit dialogs for {@link ActionObject}s.
 * Creates an appropriate builder ({@link com.oskopek.transport.model.problem.builder}) in the background and
 * later should build and call the update callback when the builder's {@link ActionObjectBuilder#update()} method
 * is called.
 *
 * @param <T> the type of the returned value
 */
public abstract class ActionObjectBuilderConsumer<T> {

    /**
     * Fills in a builder and passes to the {@link #createInternal(ActionObjectBuilder)} method.
     *
     * @param location the location
     * @param updateCallback the update callback
     * @return the return value of {@code createInternal}
     */
    public T create(Location location, Consumer<Location> updateCallback) {
        LocationBuilder builder = new LocationBuilder();
        builder.from(location, updateCallback);
        return createInternal(builder);
    }

    /**
     * Fills in a builder and passes to the {@link #createInternal(ActionObjectBuilder)} method.
     *
     * @param roadEdge the roadEdge
     * @param updateCallback the update callback
     * @return the return value of {@code createInternal}
     */
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

    /**
     * Fills in a builder and passes to the {@link #createInternal(ActionObjectBuilder)} method.
     *
     * @param vehicle the vehicle
     * @param updateCallback the update callback
     * @return the return value of {@code createInternal}
     */
    public T create(Vehicle vehicle, Consumer<Vehicle> updateCallback) {
        VehicleBuilder builder = new VehicleBuilder();
        builder.from(vehicle, updateCallback);
        return createInternal(builder);
    }

    /**
     * Fills in a builder and passes to the {@link #createInternal(ActionObjectBuilder)} method.
     *
     * @param pkg the package
     * @param updateCallback the update callback
     * @return the return value of {@code createInternal}
     */
    public T create(Package pkg, Consumer<Package> updateCallback) {
        PackageBuilder builder = new PackageBuilder();
        builder.from(pkg, updateCallback);
        return createInternal(builder);
    }

    /**
     * Tries to fill in a builder and passes to the {@link #createInternal(ActionObjectBuilder)} method.
     *
     * @param problem the problem to find the locatable in
     * @param name the locatable's name
     * @param updateCallback the update callback
     * @return the return value of {@code createInternal} or null if no locatable was found with given name
     */
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

    /**
     * Internal domain-specific method. Takes a filled-in builder created in the create methods and returns anything,
     * from the point of view of this abstract class.
     *
     * @param builder the builder
     * @return the returned value
     */
    protected abstract T createInternal(ActionObjectBuilder<?> builder);

}
