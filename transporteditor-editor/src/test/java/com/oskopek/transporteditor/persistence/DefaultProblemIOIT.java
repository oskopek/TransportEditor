/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultProblemIOIT {

    private static VariableDomain variableDomainTemp;
    private static SequentialDomain sequentialDomain;
    private static String seqProblemFileContents;
    private static String tempProblemFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        variableDomainTemp = new VariableDomainIO().parse(TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("variableDomainTemp.pddl")).stream()
                .collect(Collectors.joining("\n")));
        sequentialDomain = new SequentialDomain("seq");
        seqProblemFileContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01SeqProblem.pddl")).stream().collect(
                Collectors.joining("\n"));
        tempProblemFileContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01TempProblem.pddl")).stream().collect(
                Collectors.joining("\n"));
    }

    @Test
    public void serializeSequential() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(sequentialDomain).parse(seqProblemFileContents);

        String serialized = new DefaultProblemIO(sequentialDomain).serialize(problem);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(seqProblemFileContents, serialized);
    }

    @Test
    public void parseSequential() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(sequentialDomain).parse(seqProblemFileContents);
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

        assertNotNull(problem.getVehicle("truck-1").getCurCapacity());
        assertEquals(2, (int) problem.getVehicle("truck-1").getCurCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getMaxCapacity());
        assertEquals(2, (int) problem.getVehicle("truck-1").getMaxCapacity().getCost());

        RoadGraph rg = problem.getRoadGraph();
        assertNotNull(rg);
        assertEquals(5, rg.getNodeCount());
        assertEquals(12, rg.getEdgeCount());
        Road road = rg.getRoadBetween(rg.getLocation("city-loc-4"), rg.getLocation("city-loc-5"));
        assertNotNull(road);
        assertNotNull(road.getLength());
        assertEquals(32, road.getLength().getCost().intValue());

        assertNotNull(problem.getPackage("package-1").getTarget());
        assertEquals("city-loc-5", problem.getPackage("package-1").getTarget().getName());
        assertNotNull(problem.getPackage("package-2").getTarget());
        assertEquals("city-loc-2", problem.getPackage("package-2").getTarget().getName());
    }

    @Test
    public void serializeTemporal() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainTemp).parse(tempProblemFileContents);

        String serialized = new DefaultProblemIO(variableDomainTemp).serialize(problem);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(tempProblemFileContents, serialized);
    }

    @Test
    public void parseTemporal() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainTemp).parse(tempProblemFileContents);
        assertNotNull(problem);
        assertEquals("transport-p01-10-city-5nodes-1000size-3degree-100mindistance-2trucks-2packagespercity-2008seed",
                problem.getName());
        assertEquals(2, problem.getAllPackages().size());
        assertEquals(2, problem.getAllVehicles().size());

        assertNotNull(problem.getVehicle("truck-2"));
        assertNotNull(problem.getVehicle("truck-2").getLocation());
        assertEquals("city-loc-4", problem.getVehicle("truck-2").getLocation().getName());

        assertNotNull(problem.getPackage("package-1"));
        assertNotNull(problem.getPackage("package-1").getSize());
        assertEquals(23, problem.getPackage("package-1").getSize().getCost().intValue());
        assertNotNull(problem.getPackage("package-1").getLocation());
        assertEquals("city-loc-3", problem.getPackage("package-1").getLocation().getName());

        assertNotNull(problem.getVehicle("truck-1").getCurCapacity());
        assertEquals(100, (int) problem.getVehicle("truck-1").getCurCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getMaxCapacity());
        assertEquals(100, (int) problem.getVehicle("truck-1").getMaxCapacity().getCost());

        RoadGraph rg = problem.getRoadGraph();
        assertNotNull(rg);
        assertEquals(5, rg.getNodeCount());
        assertEquals(12, rg.getEdgeCount());
        Road road = rg.getRoadBetween(rg.getLocation("city-loc-3"), rg.getLocation("city-loc-4"));
        assertNotNull(road);
        assertEquals(45, road.getLength().getCost().intValue());
        assertEquals(FuelRoad.class, road.getClass());
        FuelRoad fuelRoad = (FuelRoad) road;
        assertEquals(89, fuelRoad.getFuelCost().getCost().intValue());
        assertEquals(45, fuelRoad.getLength().getCost().intValue());

        assertEquals(Vehicle.class, problem.getVehicle("truck-1").getClass());
        Vehicle truck1 = (Vehicle) problem.getVehicle("truck-1");
        assertNotNull(truck1.getCurFuelCapacity());
        assertEquals(424, (int) truck1.getCurFuelCapacity().getCost());
        assertNotNull(truck1.getMaxFuelCapacity());
        assertEquals(424, (int) truck1.getMaxFuelCapacity().getCost());
    }

}
