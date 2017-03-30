package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class VehicleTest {

    @Test
    public void equalsTest() throws Exception {
        Location location = new Location("l1", 0, 0);
        ActionCost zero = ActionCost.valueOf(0);
        Vehicle v1 = new Vehicle("v1", location, null, zero, zero, true, new ArrayList<>());
        Vehicle v2 = new Vehicle("v1", location, null, zero, zero, true, new ArrayList<>());
        assertEquals(v1, v2);
        assertEquals(v2, v1);
    }
}
