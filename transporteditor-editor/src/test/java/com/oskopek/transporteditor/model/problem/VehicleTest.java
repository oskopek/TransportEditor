/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import static org.junit.Assert.*;
import org.junit.Test;

public class VehicleTest {
    @Test
    public void nonNullProperties() throws Exception {
        Vehicle vehicle = new Vehicle(null, null, null, null, null);
        assertNotNull(vehicle.nameProperty());
        assertNotNull(vehicle.locationProperty());
        assertNotNull(vehicle.curCapacityProperty());
        assertNotNull(vehicle.maxCapacityProperty());
        assertNotNull(vehicle.packageListProperty());
    }
}
