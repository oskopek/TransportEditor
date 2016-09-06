/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SequentialPlanIOIT {

    private static final String sequentialPlanFile = "p01SeqPlan.val";
    private static final SequentialDomain domain = new SequentialDomain("Transport sequential");
    private static final DefaultProblem problem = P01SequentialProblem();
    private static String P01SequentialPlanFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        P01SequentialPlanFileContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(sequentialPlanFile)).stream().collect(
                Collectors.joining("\n"));
    }

    public static SequentialPlan P01SequentialPlan(DefaultProblem p01) {
        List<Action> planEntryList = new ArrayList<>();
        planEntryList.add(domain.buildPickUp()
                .build(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-3"),
                        p01.getPackage("package-1")));
        planEntryList.add(domain.buildPickUp()
                .build(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-3"),
                        p01.getPackage("package-2")));
        planEntryList.add(domain.buildDrive()
                .build(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-3"),
                        p01.getRoadGraph().getLocation("city-loc-2"), p01.getRoadGraph().getLocation("city-loc-2"),
                        p01.getRoadGraph()));
        planEntryList.add(domain.buildDrop()
                .build(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-2"),
                        p01.getPackage("package-2")));
        planEntryList.add(domain.buildDrop()
                .build(p01.getVehicle("truck-1"), p01.getRoadGraph().getLocation("city-loc-2"),
                        p01.getPackage("package-1")));
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

        Vehicle vehicle1 = new Vehicle("truck-1", graph.getLocation("city-loc-4"), ActionCost.valueOf(2),
                ActionCost.valueOf(2), new ArrayList<>());
        Vehicle vehicle2 = new Vehicle("truck-2", graph.getLocation("city-loc-5"), ActionCost.valueOf(4),
                ActionCost.valueOf(4), new ArrayList<>());
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

        return new DefaultProblem(graph, vehicles, packages);
    }

    @Test
    public void serialize() throws Exception {
        SequentialPlan P01SequentialPlan = P01SequentialPlan(problem);
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(problem);
        String serializedPlan = sequentialPlanIO.serialize(P01SequentialPlan);
        assertNotNull(serializedPlan);
        assertEquals(P01SequentialPlanFileContents, serializedPlan);
    }

    @Test
    public void parse() throws Exception {
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(problem);
        SequentialPlan plan = sequentialPlanIO.parse(P01SequentialPlanFileContents);
        assertNotNull(plan);
        assertEquals(plan, P01SequentialPlan(problem));
    }
}
