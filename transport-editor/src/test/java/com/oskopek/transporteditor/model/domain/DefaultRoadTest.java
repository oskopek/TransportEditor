package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.DefaultRoad;
import com.oskopek.transporteditor.model.problem.Road;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultRoadTest {

    @Test
    public void testEquals() throws Exception {
        Road r1 = new DefaultRoad("r1", ActionCost.valueOf(100));
        Road r2 = new DefaultRoad("r1", ActionCost.valueOf(100));
        assertEquals(r1, r1);
        assertEquals(r1, r2);
        assertEquals(r2, r1);
    }
}
