/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class DriveBuilder implements ActionBuilder<Drive, Vehicle, Location> {

    private final List<Predicate> preconditions;
    private final List<Predicate> effects;

    public DriveBuilder(List<Predicate> preconditions, List<Predicate> effects) {
        this.preconditions = preconditions;
        this.effects = effects;
    }

    @Override
    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what) {
        return build(who, where, what, null);
    }

    public <Who_ extends Vehicle, What_ extends Location> Drive build(Who_ who, Location where, What_ what, Road road) {
        return new Drive(who, where, what, preconditions, effects, road);
    }
}
