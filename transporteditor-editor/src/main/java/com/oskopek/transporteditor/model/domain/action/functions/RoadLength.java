package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Road;

public class RoadLength extends DefaultFunction {

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
