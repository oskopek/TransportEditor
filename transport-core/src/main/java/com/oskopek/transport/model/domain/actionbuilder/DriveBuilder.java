package com.oskopek.transport.model.domain.actionbuilder;

import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Road;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.Vehicle;

import java.util.List;

/**
 * Builder for the drive action.
 */
public class DriveBuilder extends DefaultActionBuilder<Drive, Vehicle, Location> {

    /**
     * Default constructor.
     *
     * @param preconditions the preconditions
     * @param effects the effects
     */
    public DriveBuilder(List<Predicate> preconditions, List<Predicate> effects) {
        super(preconditions, effects);
    }

    /**
     * <strong>Unsupported:</strong> use a different build method provided. Doesn't supply enough information for
     * this builder.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what) {
        throw new UnsupportedOperationException("Use the other build methods.");
    }

    /**
     * Build the drive action from the supplied arguments according to this template.
     *
     * @param who the vehicle
     * @param where the source location
     * @param what the destination location
     * @param road the road that was used
     * @param <Who_> the vehicle type
     * @param <What_> the location type
     * @param isFuel is the domain a fuel-enabled one
     * @return the built action
     */
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what, Road road,
            boolean isFuel) {
        return new Drive(who, where, what, getPreconditions(), getEffects(), road, isFuel);
    }

    /**
     * Build the drive action from the supplied arguments according to this template.
     * Searches in the supplied road graph for the shortest edge between the two locations.
     *
     * @deprecated Should not be used in performance-sensitive code.
     * @param who the vehicle
     * @param where the source location
     * @param what the destination location
     * @param graph the graph to search for the shortest path between the two locations
     * @param <Who_> the vehicle type
     * @param <What_> the location type
     * @param isFuel is the domain a fuel-enabled one
     * @return the built action
     */
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what,
            RoadGraph graph, boolean isFuel) {
        if (where == null) {
            throw new IllegalArgumentException("Where cannot be null.");
        }
        if (what == null) {
            throw new IllegalArgumentException("What cannot be null.");
        }

        Road road = graph.getShortestRoadBetween(where, what);
        if (road == null) {
            throw new IllegalArgumentException(
                    "Could not find road \"" + where.getName() + "\" -> \"" + what.getName() + "\".");
        }
        return build(who, where, what, road, isFuel);
    }
}
