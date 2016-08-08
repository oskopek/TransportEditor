/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import static org.junit.Assert.*;
import org.junit.Test;

public class DefaultRoadTest {

    @Test
    public void nonNullProperties() {
        Road road = new DefaultRoad(null);
        assertNotNull(road.lengthProperty());
    }

}
