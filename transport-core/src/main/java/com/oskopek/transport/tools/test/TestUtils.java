package com.oskopek.transport.tools.test;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.domain.action.predicates.*;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.persistence.IOUtils;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testing utility methods.
 */
public final class TestUtils {

    /**
     * Empty constructor.
     */
    private TestUtils() {
        // intentionally empty
    }

    /**
     * Assert that the given problem is equal to the P01 problem from seq-sat IPC08.
     * @param domain the domain
     * @param problem the problem
     */
    public static void assertP01Sequential(Domain domain, Problem problem) {
        assertNotNull(problem);
        assertEquals("transport-city-sequential-5nodes-1000size-2degree-100mindistance-2trucks-2packages-2008seed",
                problem.getName());
        assertEquals(2, problem.getAllPackages().size());
        assertEquals(2, problem.getAllVehicles().size());

        assertNotNull(problem.getVehicle("truck-2"));
        assertNotNull(problem.getVehicle("truck-2").getLocation());
        assertEquals("city-loc-5", problem.getVehicle("truck-2").getLocation().getName());

        assertNotNull(problem.getPackage("package-1"));
        assertNotNull(problem.getPackage("package-1").getLocation());
        assertEquals("city-loc-4", problem.getPackage("package-1").getLocation().getName());
        assertNotNull(problem.getPackage("package-1").getSize());
        assertEquals(1, problem.getPackage("package-1").getSize().getCost());

        assertNotNull(problem.getVehicle("truck-1").getCurCapacity());
        assertEquals(2, problem.getVehicle("truck-1").getCurCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getMaxCapacity());
        assertEquals(2, problem.getVehicle("truck-1").getMaxCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getPackageList());
        assertEquals(0, problem.getVehicle("truck-1").getPackageList().size());

        RoadGraph rg = problem.getRoadGraph();
        assertNotNull(rg);
        assertEquals(5, rg.getNodeCount());
        assertEquals(12, rg.getEdgeCount());
        Road road = rg.getShortestRoadBetween(rg.getLocation("city-loc-4"), rg.getLocation("city-loc-5"));
        assertNotNull(road);
        assertNotNull(road.getLength());
        assertEquals(32, road.getLength().getCost());
        for (int i = 1; i <= 5; i++) {
            if (domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                assertNotNull(rg.getLocation("city-loc-" + i).getPetrolStation());
            } else {
                assertNull(rg.getLocation("city-loc-" + i).getPetrolStation());
            }
            assertFalse(rg.getLocation("city-loc-" + i).hasPetrolStation());
        }

        assertNotNull(problem.getPackage("package-1").getTarget());
        assertEquals("city-loc-5", problem.getPackage("package-1").getTarget().getName());
        assertNotNull(problem.getPackage("package-2").getTarget());
        assertEquals("city-loc-2", problem.getPackage("package-2").getTarget().getName());
    }

    /**
     * Asserts that the two PDDL strings are equal. Ignores whitespace and comments.
     *
     * @param contents the expected PDDL content
     * @param serialized the serialized PDDL content for comparison
     */
    public static void assertPDDLContentEquals(String contents, String serialized) {
        String contentComp = contents.replaceAll(";.*", "").trim();
        String serializedComp = serialized.replaceAll(";.*", "").trim();

        String noSpaceContentComp = contentComp.replaceAll("\\s+", "");
        String noSpaceSerializedComp = serializedComp.replaceAll("\\s+", "");

        assertEquals(noSpaceContentComp, noSpaceSerializedComp);
    }

    /**
     * Assert that the given domain is equal to transport-strips from IPC08.
     * @param parsed the domain
     */
    public static void assertSequentialDomain(Domain parsed) {
        assertNotNull(parsed);

        assertThat(parsed.getPddlLabels()).contains(PddlLabel.Capacity);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.MaxCapacity);
        assertThat(parsed.getPddlLabels()).doesNotContain(PddlLabel.Temporal);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.ActionCost);
        assertThat(parsed.getPddlLabels()).doesNotContain(PddlLabel.Fuel);

        assertEquals(ActionCost.ONE, parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.ONE, parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.ONE, parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.ONE, parsed.getPickUpBuilder().build(null, null, null).getDuration());

        RoadGraph roadGraph = new DefaultRoadGraph("");
        roadGraph.addLocation(new Location("a", 0, 0));
        roadGraph.addLocation(new Location("b", 0, 0));
        roadGraph.addRoad(new DefaultRoad("a->b", ActionCost.valueOf(11)), roadGraph.getLocation("a"),
                roadGraph.getLocation("b"));

        assertEquals(ActionCost.valueOf(11), parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"),
                roadGraph.getLocation("b"), roadGraph, false).getCost());
        assertEquals(ActionCost.valueOf(11), parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"),
                roadGraph.getLocation("b"), roadGraph, false).getDuration());

        // drive
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(new WhoAtWhere());
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(new IsRoad());
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(new HasFuelCapacityForDrive());
        assertEquals(3, parsed.getDriveBuilder().getPreconditions().size());

        assertThat(parsed.getDriveBuilder().getEffects()).contains(new Not(new WhoAtWhere()));
        assertThat(parsed.getDriveBuilder().getEffects()).contains(new WhoAtWhat());
        assertEquals(2, parsed.getDriveBuilder().getEffects().size());

        // pickup
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(new WhoAtWhere());
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(new WhatAtWhere());
        assertEquals(2, parsed.getPickUpBuilder().getPreconditions().size());

        assertThat(parsed.getPickUpBuilder().getEffects()).contains(new In());
        assertThat(parsed.getPickUpBuilder().getEffects()).contains(new Not(new WhatAtWhere()));
        assertEquals(2, parsed.getPickUpBuilder().getEffects().size());

        // drop
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(new WhoAtWhere());
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(new In());
        assertEquals(2, parsed.getDropBuilder().getPreconditions().size());

        assertThat(parsed.getDropBuilder().getEffects()).contains(new Not(new In()));
        assertThat(parsed.getDropBuilder().getEffects()).contains(new WhatAtWhere());
        assertEquals(2, parsed.getDropBuilder().getEffects().size());

        assertNull(parsed.getRefuelBuilder());
        SequentialDomain sequentialDomain = new SequentialDomain("Transport sequential");
        assertEquals(sequentialDomain, parsed);
    }


    /**
     * Gets the contents of a file in the test resource persistence directory (usually problems, domain, plans, ...).
     *
     * @param name the name of the file, relative to the persistence directory
     * @return the contents of the text file
     * @throws IOException if an error during reading occurs
     */
    public static String getPersistenceTestFile(String name) throws IOException {
        return IOUtils.concatReadAllLines(Files.newInputStream(
                Paths.get("../transport-core/src/test/resources/com/oskopek/transport/persistence/" + name)));
    }

    /**
     * Creates the seq-sat IPC08 p01 problem optimal plan programatically.
     *
     * @return the p01 problem plan
     */
    public static SequentialPlan P01SequentialPlan() {
        SequentialDomain domain = new SequentialDomain("");
        Problem p01 = TestUtils.P01SequentialProblem();
        List<Action> planEntryList = new ArrayList<>();
        planEntryList.add(domain.buildPickUp(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-4"),
                p01.getPackage("package-1")));
        planEntryList.add(domain.buildPickUp(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-4"),
                p01.getPackage("package-2")));
        planEntryList.add(domain.buildDrive(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-4"),
                p01.getRoadGraph().getLocation("city-loc-5"), p01.getRoadGraph()));
        planEntryList.add(domain.buildDrop(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-5"),
                p01.getPackage("package-1")));
        planEntryList.add(domain.buildDrive(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-5"),
                p01.getRoadGraph().getLocation("city-loc-2"), p01.getRoadGraph()));
        planEntryList.add(domain.buildDrop(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-2"),
                p01.getPackage("package-2")));
        return new SequentialPlan(planEntryList);
    }

    /**
     * Creates the seq-sat IPC08 p01 problem programatically.
     *
     * @return the p01 problem
     */
    public static DefaultProblem P01SequentialProblem() {
        String name = "transport-city-sequential-5nodes-1000size-2degree-100mindistance-2trucks-2packages-2008seed";
        RoadGraph graph = new DefaultRoadGraph("test");
        graph.addLocation(new Location("city-loc-1", 456, 221));
        graph.addLocation(new Location("city-loc-2", 742, 542));
        graph.addLocation(new Location("city-loc-3", 564, 783));
        graph.addLocation(new Location("city-loc-4", 273, 425));
        graph.addLocation(new Location("city-loc-5", 566, 552));

        graph.addRoad(new DefaultRoad("l3l2", ActionCost.valueOf(30)), graph.getLocation("city-loc-3"),
                graph.getLocation("city-loc-2"));
        graph.addRoad(new DefaultRoad("l2l3", ActionCost.valueOf(30)), graph.getLocation("city-loc-2"),
                graph.getLocation("city-loc-3"));

        graph.addRoad(new DefaultRoad("l4l1", ActionCost.valueOf(28)), graph.getLocation("city-loc-4"),
                graph.getLocation("city-loc-1"));
        graph.addRoad(new DefaultRoad("l1l4", ActionCost.valueOf(28)), graph.getLocation("city-loc-1"),
                graph.getLocation("city-loc-4"));

        graph.addRoad(new DefaultRoad("l5l1", ActionCost.valueOf(35)), graph.getLocation("city-loc-5"),
                graph.getLocation("city-loc-1"));
        graph.addRoad(new DefaultRoad("l1l5", ActionCost.valueOf(35)), graph.getLocation("city-loc-1"),
                graph.getLocation("city-loc-5"));

        graph.addRoad(new DefaultRoad("l5l2", ActionCost.valueOf(18)), graph.getLocation("city-loc-5"),
                graph.getLocation("city-loc-2"));
        graph.addRoad(new DefaultRoad("l2l5", ActionCost.valueOf(18)), graph.getLocation("city-loc-2"),
                graph.getLocation("city-loc-5"));

        graph.addRoad(new DefaultRoad("l5l3", ActionCost.valueOf(24)), graph.getLocation("city-loc-5"),
                graph.getLocation("city-loc-3"));
        graph.addRoad(new DefaultRoad("l3l5", ActionCost.valueOf(24)), graph.getLocation("city-loc-3"),
                graph.getLocation("city-loc-5"));

        graph.addRoad(new DefaultRoad("l5l4", ActionCost.valueOf(32)), graph.getLocation("city-loc-5"),
                graph.getLocation("city-loc-4"));
        graph.addRoad(new DefaultRoad("l4l5", ActionCost.valueOf(32)), graph.getLocation("city-loc-4"),
                graph.getLocation("city-loc-5"));

        Vehicle vehicle1 = new Vehicle("truck-1", graph.getLocation("city-loc-4"), null, ActionCost.valueOf(2),
                ActionCost.valueOf(2), true, new ArrayList<>());
        Vehicle vehicle2 = new Vehicle("truck-2", graph.getLocation("city-loc-5"), null, ActionCost.valueOf(4),
                ActionCost.valueOf(4), true, new ArrayList<>());
        Map<String, Vehicle> vehicles = new HashMap<>();
        vehicles.put(vehicle1.getName(), vehicle1);
        vehicles.put(vehicle2.getName(), vehicle2);

        Package package1 = new Package("package-1", graph.getLocation("city-loc-4"), graph.getLocation("city-loc-5"),
                ActionCost.ONE);
        Package package2 = new Package("package-2", graph.getLocation("city-loc-4"), graph.getLocation("city-loc-2"),
                ActionCost.ONE);
        Map<String, Package> packages = new HashMap<>();
        packages.put(package1.getName(), package1);
        packages.put(package2.getName(), package2);

        return new DefaultProblem(name, graph, vehicles, packages);
    }

}
