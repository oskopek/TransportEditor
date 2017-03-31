package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class DefaultRoadGraphTest {

    private RoadGraph graph;

    @Before
    public void setUp() throws Exception {
        graph = new DefaultRoadGraph("test");
    }

    @Test
    public void addDuplicateLocation() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertThatThrownBy(() -> graph.addLocation(new Location("l1", 0, 0)))
                .hasMessageContaining("already in use");
        assertThatThrownBy(() -> graph.addLocation(new Location("l1", 0, 2)))
                .hasMessageContaining("already in use");
    }

    @Test
    public void getLocation() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertThat(graph.getLocation("l1")).isNotNull();
        assertThat(graph.getLocation("l2")).isNotNull();
        assertThat(graph.getLocation("l1")).isNotEqualTo(graph.getLocation("l2"));
        assertThat(graph.getLocation("l1")).isEqualTo(graph.getLocation("l1"));
    }

    @Test
    public void getAllLocations() throws Exception {
        assertThat(graph.getAllLocations()).isEmpty();
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertThat(graph.getAllLocations()).hasSize(1);
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertThat(graph.getAllLocations()).hasSize(2);
    }

    @Test
    public void getAllRoads() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertThat(graph.getAllRoads()).isEmpty();

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertThat(graph.getAllRoads()).isNotEmpty().hasSize(1).allMatch(r -> r.getRoad().equals(road1));
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

        assertThatThrownBy(() -> graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")))
                .hasMessageContaining("already in use");
        assertThatThrownBy(() -> graph.addRoad(road1, graph.getLocation("l2"), graph.getLocation("l1")))
                .hasMessageContaining("already in use");
        assertNotNull(graph.addRoad(new DefaultRoad("road2", ActionCost.valueOf(1)),
                graph.getLocation("l1"), graph.getLocation("l2")));
        assertThat(graph.getAllRoads().count()).isEqualTo(2);
    }

    @Test
    public void putRoad() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));

        assertNull(graph.getRoad("road1"));
        assertNotNull(graph.putRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.getRoad("road1"));
        assertEquals(road1, graph.getRoad("road1"));
        assertNotNull(graph.getEdge("road1"));

        assertNotNull(graph.putRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertThat(graph.getAllRoads().count()).isEqualTo(1);
        assertNotNull(graph.putRoad(road1, graph.getLocation("l2"), graph.getLocation("l1")));
        assertThat(graph.getAllRoads().count()).isEqualTo(1);
        assertThat(graph.getAllRoads().findAny().orElseThrow(IllegalStateException::new))
                .matches(r -> r.getFrom().getName().equals("l2") && r.getRoad().getName().equals("road1"));
    }

    @Test
    public void getAllRoadsBetween() throws Exception {
        Location l1 = new Location("l1", 0, 0);
        Location l2 = new Location("l2", 0, 0);
        assertNotNull(graph.addLocation(l1));
        assertNotNull(graph.addLocation(l2));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));

        assertThat(graph.getAllRoadsBetween(l1, l2)).isEmpty();
        assertThat(graph.getAllRoadsBetween(l2, l1)).isEmpty();
        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertThat(graph.getAllRoadsBetween(l1, l2)).isNotNull().hasSize(1).element(0).isEqualTo(road1);
        assertThat(graph.getAllRoadsBetween(l2, l1)).isEmpty();
    }

    @Test
    public void getRoad() throws Exception {
        assertThat(graph.getRoad("road1")).isNull();
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertThat(graph.getRoad("road1"))
                .matches(r -> r.getLength().getCost() == 30 && r.getName().equals("road1"));
    }

    @Test
    public void equalsTest() throws Exception {
        RoadGraph g1 = new DefaultRoadGraph("test");
        RoadGraph g2 = new DefaultRoadGraph("test");
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

    @Test
    public void removeLocationComplicated() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        Road road2 = new DefaultRoad("road2", ActionCost.valueOf(30));

        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.addRoad(road2, graph.getLocation("l2"), graph.getLocation("l3")));

        graph.removeLocation(graph.getLocation("l2"));
        assertThat(graph.getAllLocations()).hasSize(2).allMatch(l -> !l.getName().equals("l2"));
        assertThat(graph.getAllRoads().count()).isEqualTo(0);
    }

    @Test
    public void removeLocationSimple() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));
        graph.removeLocation(graph.getLocation("l3"));
        assertThat(graph.getAllLocations().map(Location::getName)).doesNotContain("l3").hasSize(2);

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));

        graph.removeLocation(graph.getLocation("l2"));
        assertThat(graph.getAllLocations()).hasSize(1).element(0).isEqualTo(graph.getLocation("l1"));
        assertThat(graph.getAllRoads().count()).isEqualTo(0);
    }

    @Test
    public void removeLocations() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        Road road2 = new DefaultRoad("road2", ActionCost.valueOf(30));

        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.addRoad(road2, graph.getLocation("l2"), graph.getLocation("l3")));

        graph.removeLocations(Arrays.asList(graph.getLocation("l1"), graph.getLocation("l3")));
        assertThat(graph.getAllLocations()).hasSize(1).element(0).isEqualTo(graph.getLocation("l2"));
        assertThat(graph.getAllRoads().count()).isEqualTo(0);
    }

    @Test
    public void removeRoad() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        Road road2 = new DefaultRoad("road2", ActionCost.valueOf(30));

        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.addRoad(road2, graph.getLocation("l2"), graph.getLocation("l3")));

        graph.removeRoad("road1");
        assertThat(graph.getAllLocations()).hasSize(3);
        assertThat(graph.getAllRoads().map(RoadEdge::getRoad).collect(Collectors.toList())).hasSize(1)
                .element(0).isEqualTo(road2);
    }

    @Test
    public void removeAllRoadsBetween() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        Road road2 = new DefaultRoad("road2", ActionCost.valueOf(30));
        Road road3 = new FuelRoad("road3", ActionCost.valueOf(15), ActionCost.valueOf(10));

        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.addRoad(road2, graph.getLocation("l2"), graph.getLocation("l3")));
        assertNotNull(graph.addRoad(road3, graph.getLocation("l2"), graph.getLocation("l3")));
        assertThat(graph.getAllRoads().collect(Collectors.toList())).hasSize(3);

        graph.removeAllRoadsBetween(graph.getLocation("l2"), graph.getLocation("l3"));
        assertThat(graph.getAllLocations()).hasSize(3);
        assertThat(graph.getAllRoads().map(RoadEdge::getRoad).collect(Collectors.toList())).hasSize(1)
                .element(0).isEqualTo(road1);
    }

    @Test
    public void getShortestRoadBetween() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        Road road2 = new DefaultRoad("road2", ActionCost.valueOf(30));
        Road road3 = new FuelRoad("road3", ActionCost.valueOf(15), ActionCost.valueOf(40));

        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.addRoad(road2, graph.getLocation("l2"), graph.getLocation("l3")));
        assertNotNull(graph.addRoad(road3, graph.getLocation("l2"), graph.getLocation("l3")));
        assertThat(graph.getAllRoads().collect(Collectors.toList())).hasSize(3);

        assertThat(graph.getShortestRoadBetween(graph.getLocation("l2"), graph.getLocation("l3"))).isEqualTo(road3);
    }

    @Test
    public void removeRoads() throws Exception {
        assertNotNull(graph.addLocation(new Location("l1", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l2", 0, 0)));
        assertNotNull(graph.addLocation(new Location("l3", 0, 0)));

        Road road1 = new DefaultRoad("road1", ActionCost.valueOf(30));
        Road road2 = new DefaultRoad("road2", ActionCost.valueOf(30));

        assertNotNull(graph.addRoad(road1, graph.getLocation("l1"), graph.getLocation("l2")));
        assertNotNull(graph.addRoad(road2, graph.getLocation("l2"), graph.getLocation("l3")));

        graph.removeRoads(Arrays.asList("road1", "road2"));
        assertThat(graph.getAllLocations()).hasSize(3);
        assertThat(graph.getAllRoads().count()).isEqualTo(0);
    }
}
