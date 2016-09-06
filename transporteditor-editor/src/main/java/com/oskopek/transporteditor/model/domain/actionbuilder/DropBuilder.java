/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.Drop;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class DropBuilder extends DefaultActionBuilder<Drop, Vehicle, Package> {

    private final ActionCost cost;
    private final ActionCost duration;

    public DropBuilder(List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super(preconditions, effects);
        this.cost = cost;
        this.duration = duration;
    }

    @Override
    public <Who_ extends Vehicle, What_ extends Package> Drop build(Who_ who, Location where, What_ what) {
        return new Drop(who, where, what, getPreconditions(), getEffects(), cost, duration);
    }
}
