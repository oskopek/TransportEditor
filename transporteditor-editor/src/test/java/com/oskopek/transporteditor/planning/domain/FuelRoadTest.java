/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.problem.FuelRoad;
import static org.junit.Assert.*;
import org.junit.Test;

public class FuelRoadTest {

    @Test
    public void nonNullProperties() {
        FuelRoad road = new FuelRoad(null, null);
        assertNotNull(road.lengthProperty());
        assertNotNull(road.fuelCostProperty());
        road = new FuelRoad(null, null);
        assertNotNull(road.lengthProperty());
        assertNotNull(road.fuelCostProperty());
    }

}
