package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oskopek.transporteditor.persistence.IOUtils.concatReadAllLines;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SequentialPlanIOIT {

    private static final SequentialDomain domain = new SequentialDomain("Transport sequential");
    public static final DefaultProblem p01Problem = P01SequentialProblem();
    public static DefaultProblem p20Problem;
    public static String P01SequentialPlanFileContents;
    public static String P20SequentialPlanFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        initialize();
    }

    /**
     * Used by other classes as well.
     *
     * @throws IOException if an error occurs during plan/problem reading
     */
    public static void initialize() throws IOException {
        p20Problem = new DefaultProblemIO(domain).parse(concatReadAllLines(SequentialPlanIOIT.class
                .getResourceAsStream("p20SeqProblem.pddl")));
        P01SequentialPlanFileContents = concatReadAllLines(SequentialPlanIOIT.class
                .getResourceAsStream("p01SeqPlan.val"));
        P20SequentialPlanFileContents = concatReadAllLines(SequentialPlanIOIT.class
                .getResourceAsStream("p20SeqPlan.val"));
    }

    public static SequentialPlan P01SequentialPlan(Problem p01) {
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

    @Test
    public void serialize() throws Exception {
        SequentialPlan P01SequentialPlan = P01SequentialPlan(p01Problem);
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(domain, p01Problem);
        String serializedPlan = sequentialPlanIO.serialize(P01SequentialPlan);
        assertNotNull(serializedPlan);
        assertEquals(P01SequentialPlanFileContents, serializedPlan);
    }

    @Test
    public void parse() throws Exception {
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(domain, p01Problem);
        SequentialPlan plan = sequentialPlanIO.parse(P01SequentialPlanFileContents);
        assertNotNull(plan);
        assertEquals(plan, P01SequentialPlan(p01Problem));
    }

    @Test
    public void parseSerializeLarge() throws Exception {
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(domain, p20Problem);
        SequentialPlan plan = sequentialPlanIO.parse(P20SequentialPlanFileContents);
        assertNotNull(plan);
        TestUtils.assertPDDLContentEquals(P20SequentialPlanFileContents, sequentialPlanIO.serialize(plan));
    }
}
