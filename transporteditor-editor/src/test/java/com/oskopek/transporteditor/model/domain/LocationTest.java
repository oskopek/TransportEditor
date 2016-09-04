/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.problem.Location;
import static org.junit.Assert.*;
import org.junit.Test;

public class LocationTest {

    @Test
    public void nonNullProperties() {
        Location location = new Location(null, null, null);
        assertNotNull(location.nameProperty());
        assertNotNull(location.xCoordinateProperty());
        assertNotNull(location.yCoordinateProperty());
    }

}
