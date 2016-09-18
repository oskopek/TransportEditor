/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.DefaultPlanningSession;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

public class DefaultPlanningSessionIOIT {

    private static String emptyPlanningSessionContents;
    private static DefaultPlanningSession emptySession = new DefaultPlanningSession();

    @BeforeClass
    public static void setUpClass() throws Exception {
        emptyPlanningSessionContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("emptyDefaultPlanningSession.xml")).stream()
                .collect(Collectors.joining("\n"));
    }

    @Test
    public void serializeEmpty() throws Exception {
        String serialized = new DefaultPlanningSessionIO().serialize(emptySession);
        TestUtils.assertPDDLContentEquals(emptyPlanningSessionContents, serialized);
    }

    @Test
    public void parseEmpty() throws Exception {
        DefaultPlanningSession parsed = new DefaultPlanningSessionIO().parse(emptyPlanningSessionContents);
        assertNotNull(parsed);
        assertNull(parsed.getDomain());
        assertNull(parsed.getPlan());
        assertNull(parsed.getProblem());
        assertNull(parsed.getPlanner());
        assertNull(parsed.getValidator());
        assertEquals(emptySession, parsed);
    }
}