/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.functions;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.Road;

public class RoadLength implements Function {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Road.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("RoadLength can only be applied to one argument of type Road");
        }
        return apply((Road) actionObjects[0]);
    }

    public ActionCost apply(Road road) {
        if (road == null) {
            throw new IllegalArgumentException("Road cannot be null.");
        }
        return road.getLength();
    }
}
