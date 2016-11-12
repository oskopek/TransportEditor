package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

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

}
