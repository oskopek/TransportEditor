package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.VariableDomain;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.oskopek.transport.persistence.IOUtils.readAllLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DefaultProblemIOIT {

    private static VariableDomain variableDomainTemp;
    private static SequentialDomain sequentialDomain;
    private static VariableDomain variableDomainFuelSeq;
    private static VariableDomain variableDomainNum;
    private static String seqProblemFileContents;
    private static String fuelSeqProblemFileContents;
    private static String tempProblemFileContents;
    private static String tempBigProblemFileContents;
    private static String numProblemFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception { // TODO: Netbenefit parse/ser test
        variableDomainTemp = new VariableDomainIO().parse(readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("variableDomainTemp.pddl")).stream()
                .collect(Collectors.joining("\n")));
        variableDomainNum = new VariableDomainIO().parse(readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("variableDomainNum.pddl")).stream()
                .collect(Collectors.joining("\n")));
        variableDomainFuelSeq = new VariableDomainIO().parse(readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("variableDomainFuelSeq.pddl")).stream()
                .collect(Collectors.joining("\n")));
        sequentialDomain = new SequentialDomain("seq");
        seqProblemFileContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01SeqProblem.pddl")).stream().collect(
                Collectors.joining("\n")) + "\n";
        fuelSeqProblemFileContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01FuelSeqProblem.pddl")).stream().collect(
                Collectors.joining("\n")) + "\n";
        tempProblemFileContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01TempProblem.pddl")).stream().collect(
                Collectors.joining("\n")) + "\n";
        tempBigProblemFileContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p30TempProblem.pddl")).stream().collect(
                Collectors.joining("\n")) + "\n";
        numProblemFileContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01NetProblem.pddl")).stream().collect(
                Collectors.joining("\n")) + "\n";
    }

    @Test
    public void serializeSequential() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(sequentialDomain).parseDefault(seqProblemFileContents);

        String serialized = new DefaultProblemIO(sequentialDomain).serialize(problem);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(seqProblemFileContents, serialized);
    }

    @Test
    @Ignore("TODO: Parse point locations from comments")
    public void serializeSequentialExact() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(sequentialDomain).parseDefault(seqProblemFileContents);

        String serialized = new DefaultProblemIO(sequentialDomain).serialize(problem);
        assertNotNull(serialized);
        assertEquals(seqProblemFileContents, serialized);
    }

    @Test
    public void parseSequential() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(sequentialDomain).parseDefault(seqProblemFileContents);
        TestUtils.assertP01Sequential(sequentialDomain, problem);
    }

    @Test
    public void parseFuelSequentialWithFuel() throws Exception {
        assertThat(variableDomainFuelSeq.getPddlLabels()).contains(PddlLabel.Fuel);
        DefaultProblem problem = new DefaultProblemIO(variableDomainFuelSeq).parseDefault(fuelSeqProblemFileContents);
        TestUtils.assertP01Sequential(variableDomainFuelSeq, problem);
        assertThat(problem.getRoadGraph().getAllLocations()).allMatch(l -> !l.hasPetrolStation());
        assertThat(problem.getRoadGraph().getAllRoads().map(RoadEdge::getRoad).map(Object::getClass))
                .allMatch(FuelRoad.class::equals);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getCurFuelCapacity() != null)
                .allMatch(v -> v.getMaxFuelCapacity() != null);
    }

    @Test
    public void parseSequentialWithFuel() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(sequentialDomain).parseDefault(fuelSeqProblemFileContents);
        TestUtils.assertP01Sequential(sequentialDomain, problem);
        assertThat(problem.getRoadGraph().getAllLocations().map(Location::getPetrolStation)).allMatch(Objects::isNull);
        assertThat(problem.getRoadGraph().getAllRoads().map(RoadEdge::getRoad).map(Object::getClass))
                .allMatch(DefaultRoad.class::equals);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getCurFuelCapacity() == null)
                .allMatch(v -> v.getMaxFuelCapacity() == null);
    }

    @Test
    public void serializeTemporal() throws Exception {
        serializeTemporalInternal(tempProblemFileContents);
    }

    @Test
    public void serializeBigTemporal() throws Exception {
        DefaultProblem problem = serializeTemporalInternal(tempBigProblemFileContents);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getTarget() != null);
        Map<String, String> truckGoals = new HashMap<>(4 + 7);
        truckGoals.put("truck-0", "hub-0");
        truckGoals.put("truck-1", "hub-1");
        truckGoals.put("truck-2", "hub-2");
        truckGoals.put("truck-3", "hub-3");
        truckGoals.put("ctruck-0-0", "hub-0");
        truckGoals.put("ctruck-1-0", "hub-1");
        truckGoals.put("ctruck-2-0", "hub-2");
        truckGoals.put("ctruck-3-0", "hub-3");
        truckGoals.put("ctruck-4-0", "hub-4");
        truckGoals.put("ctruck-5-0", "hub-5");
        truckGoals.put("ctruck-6-0", "hub-6");
        truckGoals.forEach((key, value) -> assertThat(problem.getVehicle(key).getTarget())
                .isEqualTo(problem.getRoadGraph().getLocation(value)));
    }

    private DefaultProblem serializeTemporalInternal(String problemFileContents) {
        DefaultProblem problem = new DefaultProblemIO(variableDomainTemp).parseDefault(problemFileContents);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getCurFuelCapacity() != null);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getMaxFuelCapacity() != null);
        String serialized = new DefaultProblemIO(variableDomainTemp).serialize(problem);
        assertNotNull(serialized);

        DefaultProblem problemAgain = new DefaultProblemIO(variableDomainTemp).parseDefault(serialized);
        assertThat(problem).isEqualTo(problemAgain);

        TestUtils.assertPDDLContentEquals(problemFileContents, serialized);
        return problem;
    }

    @Test
    @Ignore("TODO: Parse point locations from comments")
    public void serializeTemporalExact() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainTemp).parseDefault(tempProblemFileContents);
        String serialized = new DefaultProblemIO(variableDomainTemp).serialize(problem);
        assertNotNull(serialized);
        assertEquals(tempProblemFileContents, serialized);
    }

    @Test
    public void parseTemporal() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainTemp).parseDefault(tempProblemFileContents);
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
        assertEquals(23, problem.getPackage("package-1").getSize().getCost());
        assertNotNull(problem.getPackage("package-1").getLocation());
        assertEquals("city-loc-3", problem.getPackage("package-1").getLocation().getName());

        assertNotNull(problem.getVehicle("truck-1").getCurCapacity());
        assertEquals(100, problem.getVehicle("truck-1").getCurCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getMaxCapacity());
        assertEquals(100, problem.getVehicle("truck-1").getMaxCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getPackageList());
        assertEquals(0, problem.getVehicle("truck-1").getPackageList().size());

        RoadGraph rg = problem.getRoadGraph();
        assertNotNull(rg);
        assertEquals(5, rg.getNodeCount());
        assertEquals(12, rg.getEdgeCount());
        Road road = rg.getShortestRoadBetween(rg.getLocation("city-loc-3"), rg.getLocation("city-loc-4"));
        assertNotNull(road);
        assertEquals(45, road.getLength().getCost());
        assertEquals(FuelRoad.class, road.getClass());
        FuelRoad fuelRoad = (FuelRoad) road;
        assertEquals(89, fuelRoad.getFuelCost().getCost());
        assertEquals(45, fuelRoad.getLength().getCost());
        assertNotNull(rg.getLocation("city-loc-1").getPetrolStation());
        assertTrue(rg.getLocation("city-loc-1").hasPetrolStation());
        for (int i = 2; i <= 5; i++) {
            assertNotNull(rg.getLocation("city-loc-" + i).getPetrolStation());
            assertFalse(rg.getLocation("city-loc-" + i).hasPetrolStation());
        }

        assertEquals(Vehicle.class, problem.getVehicle("truck-1").getClass());
        Vehicle truck1 = problem.getVehicle("truck-1");
        assertNotNull(truck1.getCurFuelCapacity());
        assertEquals(424, truck1.getCurFuelCapacity().getCost());
        assertNotNull(truck1.getMaxFuelCapacity());
        assertEquals(424, truck1.getMaxFuelCapacity().getCost());
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getTarget() == null);
    }

    @Test
    @Ignore("Numeric tests are ignored for now")
    public void serializeNumeric() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainNum).parseDefault(numProblemFileContents);
        String serialized = new DefaultProblemIO(variableDomainNum).serialize(problem);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(numProblemFileContents, serialized);
    }

    @Test
    @Ignore("TODO: Parse point locations from comments")
    public void serializeNumericExact() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainNum).parseDefault(numProblemFileContents);
        String serialized = new DefaultProblemIO(variableDomainNum).serialize(problem);
        assertNotNull(serialized);
        assertEquals(numProblemFileContents, serialized);
    }

    @Test
    @Ignore("Numeric tests are ignored for now")
    public void parseNumeric() throws Exception {
        DefaultProblem problem = new DefaultProblemIO(variableDomainNum).parseDefault(numProblemFileContents);
        assertNotNull(problem);
        assertEquals(
                "transport-city-netbenefit-0petrol-station-6nodes-1000size-3degree-100mindistance-2trucks"
                        + "-2packagespercity-2008seed",
                problem.getName());
        assertEquals(2, problem.getAllPackages().size());
        assertEquals(2, problem.getAllVehicles().size());

        assertNotNull(problem.getVehicle("truck-2"));
        assertNotNull(problem.getVehicle("truck-2").getLocation());
        assertEquals("city-loc-3", problem.getVehicle("truck-2").getLocation().getName());

        assertNotNull(problem.getPackage("package-1"));
        assertNotNull(problem.getPackage("package-1").getSize());
        assertEquals(1, problem.getPackage("package-1").getSize().getCost());
        assertNotNull(problem.getPackage("package-1").getLocation());
        assertEquals("city-loc-5", problem.getPackage("package-1").getLocation().getName());

        assertNotNull(problem.getVehicle("truck-1").getCurCapacity());
        assertEquals(3, problem.getVehicle("truck-1").getCurCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getMaxCapacity());
        assertEquals(3, problem.getVehicle("truck-1").getMaxCapacity().getCost());
        assertNotNull(problem.getVehicle("truck-1").getPackageList());
        assertEquals(0, problem.getVehicle("truck-1").getPackageList().size());

        RoadGraph rg = problem.getRoadGraph();
        assertNotNull(rg);
        assertEquals(6, rg.getNodeCount());
        assertEquals(14, rg.getEdgeCount());
        Road road = rg.getShortestRoadBetween(rg.getLocation("city-loc-3"), rg.getLocation("city-loc-4"));
        assertNotNull(road);
        assertEquals(45, road.getLength().getCost());
        assertEquals(FuelRoad.class, road.getClass());
        FuelRoad fuelRoad = (FuelRoad) road;
        assertEquals(89, fuelRoad.getFuelCost().getCost());
        assertEquals(45, fuelRoad.getLength().getCost());

        assertEquals(Vehicle.class, problem.getVehicle("truck-1").getClass());
        Vehicle truck1 = problem.getVehicle("truck-1");
        assertNotNull(truck1.getCurFuelCapacity());
        assertEquals(323, truck1.getCurFuelCapacity().getCost());
        assertNotNull(truck1.getMaxFuelCapacity());
        assertEquals(323, truck1.getMaxFuelCapacity().getCost());
    }

}
