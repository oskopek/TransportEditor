package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RoadGraphTest {

    private RoadGraph graph;

    @Before
    public void setUp() throws Exception {
        graph = new RoadGraph("test");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void addLocation() throws Exception {

    }

    @Test
    public void getLocation() throws Exception {

    }

    @Test
    public void getAllLocations() throws Exception {

    }

    @Test
    public void getAllRoads() throws Exception {

    }

    @Test
    public void addRoad() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));

        assertNull(graph.getRoad("road1"));
        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.getRoad("road1"));
        assertEquals(road1, graph.getRoad("road1"));
        assertNotNull(graph.getEdge("road1"));
    }

    @Test
    public void getRoadBetween() throws Exception {
        Location l1 = new Location("l1", 0, 0);
        Location l2 = new Location("l2", 0, 0);
        assertNotNull(graph.addLocation(l1));
        assertNotNull(graph.addLocation(l2));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));

        assertNull(graph.getRoadBetween(l1, l2));
        assertNull(graph.getRoadBetween(l2, l1));
        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.getRoadBetween(l1, l2));
        assertEquals(road1, graph.getRoadBetween(l1, l2));
        assertNull(graph.getRoadBetween(l2, l1));
    }

    @Test
    public void getRoad() throws Exception {

    }

    // TODO: Road graph test

    @Test
    public void equalsTest() throws Exception {
        RoadGraph g1 = new RoadGraph("test");
        RoadGraph g2 = new RoadGraph("test");
        assertEquals(g1, g2);
        Location a = new Location("a", 0, 0);
        Location b = new Location("b", 0, 1);
        g1.addLocation(a);
        g1.addLocation(b);
        g2.addLocation(a);
        g2.addLocation(b);
        assertEquals(g1, g2);
        g1.addRoad(new DefaultRoad("r1", ActionCost.valueOf(100)), a, b);
        g2.addRoad(new DefaultRoad("r1", ActionCost.valueOf(100)), a, b);
        assertEquals(g1, g2);
        g2.addRoad(new DefaultRoad("r2", ActionCost.valueOf(100)), b, a);
        assertNotEquals(g1, g2);
        g1.addRoad(new DefaultRoad("r2", ActionCost.valueOf(100)), b, a);
        assertEquals(g1, g2);
        g2.addLocation(new Location("c", 0, 0));
        assertNotEquals(g1, g2);
    }

}
