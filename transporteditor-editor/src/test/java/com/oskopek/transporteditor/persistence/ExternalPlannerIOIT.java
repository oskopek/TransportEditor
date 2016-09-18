/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

public class ExternalPlannerIOIT {

    private static String emptyPlannerContents;
    private static ExternalPlanner emptyPlanner = new ExternalPlanner("planner.jar -domain {0} -problem {1}");

    @BeforeClass
    public static void setUpClass() throws Exception {
        emptyPlannerContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("emptyExternalPlanner.xml")).stream().collect(
                Collectors.joining("\n"));
    }

    @Test
    public void serializeEmpty() throws Exception {
        String serialized = new ExternalPlannerIO().serialize(emptyPlanner);
        TestUtils.assertPDDLContentEquals(emptyPlannerContents, serialized);
    }

    @Test
    public void parseEmpty() throws Exception {
        ExternalPlanner parsed = new ExternalPlannerIO().parse(emptyPlannerContents);
        assertNotNull(parsed);
        assertNull(parsed.getBestPlan());
        assertEquals(emptyPlanner, parsed);
    }
}
