/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import static org.junit.Assert.*;
import org.junit.Test;

public class FuelVehicleTest {

    @Test
    public void nonNullProperty() throws Exception {
        FuelVehicle vehicle = new FuelVehicle(null, null, null, null, null, null, null);
        assertNotNull(vehicle.curFuelCapacityProperty());
        assertNotNull(vehicle.maxFuelCapacityProperty());
    }

}
