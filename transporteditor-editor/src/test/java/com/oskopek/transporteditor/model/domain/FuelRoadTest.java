/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.problem.FuelRoad;
import static org.junit.Assert.*;
import org.junit.Test;

public class FuelRoadTest {

    @Test
    public void nonNullProperties() {
        FuelRoad road = new FuelRoad(null, null);
        assertNotNull(road.lengthProperty());
        assertNotNull(road.fuelCostProperty());
    }

}
