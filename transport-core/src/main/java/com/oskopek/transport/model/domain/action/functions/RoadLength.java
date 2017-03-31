package com.oskopek.transport.model.domain.action.functions;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Road;

/**
 * The road-length function. Returns the the road length of the {@link Road}.
 */
public class RoadLength extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Road.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("RoadLength can only be applied to one argument of type Road");
        }
        return apply((Road) actionObjects[0]);
    }

    /**
     * Get the length of the {@link Road}.
     *
     * @param road the road
     * @return the length
     */
    public ActionCost apply(Road road) {
        if (road == null) {
            throw new IllegalArgumentException("Road cannot be null.");
        }
        return road.getLength();
    }
}
