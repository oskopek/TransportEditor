/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class DriveBuilder extends DefaultActionBuilder<Drive, Vehicle, Location> {

    public DriveBuilder(List<Predicate> preconditions, List<Predicate> effects) {
        super(preconditions, effects);
    }

    @Override
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what) {
        throw new UnsupportedOperationException("Use the other build methods.");
        //return build(who, where, what, null); // TODO: throw an exception, rather?
    }

    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what, Road road) {
        return new Drive(who, where, what, getPreconditions(), getEffects(), road);
    }

    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what,
            Location fromWhere, RoadGraph graph) {
        if (fromWhere == null) {
            throw new IllegalArgumentException("From where cannot be null.");
        }
        if (where == null) {
            throw new IllegalArgumentException("Where cannot be null.");
        }

        Road road = graph.getRoadBetween(fromWhere, where);
        if (road == null) {
            throw new IllegalArgumentException(
                    "Could not find road \"" + fromWhere.getName() + "\" -> \"" + where.getName() + "\".");
        }
        return build(who, where, what, road);
    }
}
