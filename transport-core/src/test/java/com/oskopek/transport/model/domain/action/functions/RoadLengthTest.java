package com.oskopek.transport.model.domain.action.functions;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.DefaultRoad;
import com.oskopek.transport.model.problem.Road;
import org.junit.Test;

public class RoadLengthTest {

    @Test
    public void returnsCorrectCost() {
        Road road = new DefaultRoad("", ActionCost.valueOf(12));
        assertEquals(ActionCost.valueOf(12), new RoadLength().apply(road));
    }

    @Test
    public void returnsCorrectCostOnGenericCall() {
        ActionObject object = new DefaultRoad("", ActionCost.valueOf(12));
        assertEquals(ActionCost.valueOf(12), new RoadLength().apply(object));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnNull() {
        new RoadLength().apply((Road) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnNullOnGenericCall() {
        new RoadLength().apply((ActionObject) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOnMultipleArguments() {
        new RoadLength().apply(new DefaultRoad("", ActionCost.valueOf(12)),
                new DefaultRoad(null, ActionCost.valueOf(1)));
    }

}
