/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.SequentialDomain;
import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.domain.action.Drive;
import com.oskopek.transporteditor.planning.domain.action.Drop;
import com.oskopek.transporteditor.planning.domain.action.PickUp;
import com.oskopek.transporteditor.planning.plan.DefaultPlanEntry;
import com.oskopek.transporteditor.planning.plan.PlanEntry;
import com.oskopek.transporteditor.planning.plan.SequentialPlan;
import com.oskopek.transporteditor.planning.problem.*;
import com.oskopek.transporteditor.planning.problem.Package;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SequentialPlanIOIT {

    private final static String sequentialPlanFile = "p01SeqPlan.val";
    private final static SequentialDomain domain = new SequentialDomain("test");
    private final static DefaultProblem problem = P01SequentialProblem();
    private static String P01SequentialPlanFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        P01SequentialPlanFileContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(sequentialPlanFile)).stream().collect(
                Collectors.joining("\n"));
    }

    public static SequentialPlan P01SequentialPlan(DefaultProblem p01) {
        List<PlanEntry> planEntryList = new ArrayList<>();
        planEntryList.add(new DefaultPlanEntry(
                new PickUp(p01.getVehicleList().get(0), p01.getRoadGraph().getLocation("city-loc-3"),
                        p01.getPackageList().get(0)), ActionCost.valueOf(0)));
        planEntryList.add(new DefaultPlanEntry(
                new PickUp(p01.getVehicleList().get(0), p01.getRoadGraph().getLocation("city-loc-3"),
                        p01.getPackageList().get(1)), ActionCost.valueOf(1)));
        planEntryList.add(new DefaultPlanEntry(
                new Drive(p01.getVehicleList().get(0), p01.getRoadGraph().getLocation("city-loc-3"),
                        p01.getRoadGraph().getLocation("city-loc-2")), ActionCost.valueOf(2)));
        planEntryList.add(new DefaultPlanEntry(
                new Drop(p01.getVehicleList().get(0), p01.getRoadGraph().getLocation("city-loc-2"),
                        p01.getPackageList().get(1)), ActionCost.valueOf(3)));
        planEntryList.add(new DefaultPlanEntry(
                new Drop(p01.getVehicleList().get(0), p01.getRoadGraph().getLocation("city-loc-2"),
                        p01.getPackageList().get(0)), ActionCost.valueOf(4)));
        return new SequentialPlan(planEntryList);
    }

    public static DefaultProblem P01SequentialProblem() {
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

        Vehicle vehicle1 = new Vehicle("v1", graph.getLocation("city-loc-4"), ActionCost.valueOf(2),
                ActionCost.valueOf(2), new ArrayList<>());
        Vehicle vehicle2 = new Vehicle("v2", graph.getLocation("city-loc-5"), ActionCost.valueOf(4),
                ActionCost.valueOf(4), new ArrayList<>());
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(vehicle1);
        vehicles.add(vehicle2);

        Package package1 = new Package("p1", graph.getLocation("city-loc-4"), graph.getLocation("city-loc-5"),
                ActionCost.valueOf(1));
        Package package2 = new Package("p2", graph.getLocation("city-loc-4"), graph.getLocation("city-loc-2"),
                ActionCost.valueOf(1));
        List<Package> packages = new ArrayList<>();
        packages.add(package1);
        packages.add(package2);

        return new DefaultProblem(graph, vehicles, packages);
    }

    @Test
    public void serialize() throws Exception {
        SequentialPlan P01SequentialPlan = P01SequentialPlan(problem);
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(problem);
        String serializedPlan = sequentialPlanIO.serialize(P01SequentialPlan);
        assertNotNull(serializedPlan);
        TestUtils.assertPDDLContentEquals(P01SequentialPlanFileContents, serializedPlan);
    }

    @Test
    public void parse() throws Exception {
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(problem);
        SequentialPlan plan = sequentialPlanIO.parse(P01SequentialPlanFileContents);
        assertNotNull(plan);
        assertEquals(plan, P01SequentialPlan(problem));
    }

}