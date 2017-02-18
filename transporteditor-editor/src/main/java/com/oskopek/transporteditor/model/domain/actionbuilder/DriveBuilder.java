package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

/**
 * Builder for the drive action.
 */
public class DriveBuilder extends DefaultActionBuilder<Drive, Vehicle, Location> {
    // TODO: Add fuel as a pre and post condition to drive

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
     * @return the built action
     */
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what, Road road) {
        return new Drive(who, where, what, getPreconditions(), getEffects(), road);
    }

    /**
     * Build the drive action from the supplied arguments according to this template.
     * Searches in the supplied road graph for the shortest edge between the two locations.
     *
     * @param who the vehicle
     * @param where the source location
     * @param what the destination location
     * @param graph the graph to search for the shortest path between the two locations
     * @param <Who_> the vehicle type
     * @param <What_> the location type
     * @return the built action
     */
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what,
            RoadGraph graph) {
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
        return build(who, where, what, road);
    }
}
