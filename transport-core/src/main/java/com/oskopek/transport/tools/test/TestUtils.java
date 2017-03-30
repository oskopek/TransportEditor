package com.oskopek.transport.tools.test;

import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.persistence.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Various methods for easier testing. Util methods missing in JUnit, Mockito, etc.
 */
public final class TestUtils {

    /**
     * Util class, hide the default constructor.
     */
    private TestUtils() {
        // intentionally empty
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

        if (!noSpaceContentComp.equals(noSpaceSerializedComp)) {
            if (!contentComp.equals(serializedComp)) {
                throw new AssertionError(contentComp + "\n\n!=\n\n" + serializedComp);
            }
        }
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
        RoadGraph graph = new RoadGraph("test");
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
                ActionCost.valueOf(1));
        Package package2 = new Package("package-2", graph.getLocation("city-loc-4"), graph.getLocation("city-loc-2"),
                ActionCost.valueOf(1));
        Map<String, Package> packages = new HashMap<>();
        packages.put(package1.getName(), package1);
        packages.put(package2.getName(), package2);

        return new DefaultProblem(name, graph, vehicles, packages);
    }

}
