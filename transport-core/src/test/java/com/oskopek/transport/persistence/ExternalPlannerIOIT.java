package com.oskopek.transport.persistence;

import com.oskopek.transport.model.planner.ExternalPlanner;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.oskopek.transport.persistence.IOUtils.readAllLines;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class ExternalPlannerIOIT {

    private static String emptyPlannerContents;
    private static String sessionContents;
    private static ExternalPlanner emptyPlanner = new ExternalPlanner("planner.jar", "-domain {0}"
            + " -problem {1} -out {2}");

    @BeforeClass
    public static void setUpClass() throws Exception {
        emptyPlannerContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("emptyExternalPlanner.xml")).stream().collect(
                Collectors.joining("\n"));
        sessionContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("emptyDefaultPlanningSession.xml")).stream().collect(
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
        assertNull(parsed.getCurrentPlan());
        assertEquals(emptyPlanner.getCurrentPlan(), parsed.getCurrentPlan());
        assertEquals(emptyPlanner, parsed);
        assertEquals(emptyPlanner.getLogListenerListUnmodifiable(), parsed.getLogListenerListUnmodifiable());
        assertNotNull(parsed.isPlanning());
        assertFalse(parsed.isPlanning().getValue());
    }

    @Test
    public void testParseFailsForExternalPlanner() throws Exception {
        assertThatThrownBy(() -> new ExternalPlannerIO().parse(sessionContents, ExternalPlanner.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Could not parse");
        assertThatThrownBy(() -> new ExternalPlannerIO().parse(sessionContents))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Could not parse");
    }
}
