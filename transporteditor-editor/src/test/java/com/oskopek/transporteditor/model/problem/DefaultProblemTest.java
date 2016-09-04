/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import static org.junit.Assert.*;
import org.junit.Test;

public class DefaultProblemTest {
    @Test
    public void notNullProperties() throws Exception {
        Problem problem = new DefaultProblem(null, null, null);
        assertNotNull(problem.roadGraphProperty());
        assertNotNull(problem.vehicleListProperty());
        assertNotNull(problem.packageListProperty());
    }
}
