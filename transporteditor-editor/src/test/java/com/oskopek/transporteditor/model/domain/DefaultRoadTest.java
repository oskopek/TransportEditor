/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.problem.DefaultRoad;
import com.oskopek.transporteditor.model.problem.Road;
import static org.junit.Assert.*;
import org.junit.Test;

public class DefaultRoadTest {

    @Test
    public void nonNullProperties() {
        Road road = new DefaultRoad(null, null);
        assertNotNull(road.lengthProperty());
    }

}
