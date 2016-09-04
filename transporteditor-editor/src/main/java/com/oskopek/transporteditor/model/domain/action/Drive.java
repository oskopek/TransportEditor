/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Road;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class Drive extends DefaultAction<Vehicle, Location> {

    private Drive(Vehicle vehicle, Location where, Location location, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("drive", vehicle, where, location, preconditions, effects, cost, duration);
    }

    public static Drive newDrive(Vehicle vehicle, Location location, Location location2, List<Predicate> preconditions,
            List<Predicate> effects, RoadGraph graph) {
        Road road = graph.getNode(location.getName()).getEdgeToward(location2.getName()).getAttribute("road");
        return new Drive(vehicle, location, location2, preconditions, effects, road.getLength(), road.getLength());
    }
}
