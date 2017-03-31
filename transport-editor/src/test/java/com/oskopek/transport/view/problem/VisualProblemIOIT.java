package com.oskopek.transport.view.problem;

import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.VariableDomain;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.persistence.VariableDomainIO;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class VisualProblemIOIT {

    private static VariableDomain variableDomainTemp;
    private static SequentialDomain sequentialDomain;
    private static VariableDomain variableDomainFuelSeq;
    private static String seqProblemFileContents;
    private static String fuelSeqProblemFileContents;
    private static String tempProblemFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        variableDomainTemp = new VariableDomainIO().parse(TestUtils.getPersistenceTestFile("variableDomainTemp.pddl"));
        variableDomainFuelSeq = new VariableDomainIO().parse(TestUtils
                .getPersistenceTestFile("variableDomainFuelSeq.pddl"));
        sequentialDomain = new SequentialDomain("seq");
        seqProblemFileContents = TestUtils.getPersistenceTestFile("p01SeqProblem.pddl");
        fuelSeqProblemFileContents = TestUtils.getPersistenceTestFile("p01FuelSeqProblem.pddl");
        tempProblemFileContents = TestUtils.getPersistenceTestFile("p01TempProblem.pddl");
    }

    @Test
    public void parseSequential() throws Exception {
        VisualProblem problem = new VisualProblemIO(sequentialDomain).parse(seqProblemFileContents);
        assertThat(problem.getVisualRoadGraph().getNodeCount()).isEqualTo(5);
        problem.getVisualRoadGraph().redrawActionObjectSprites(problem);
        assertThat(problem.getVisualRoadGraph().getAttributeKeySet().stream().anyMatch(a -> a.startsWith("ui.sprite")))
                .isTrue();
        TestUtils.assertP01Sequential(sequentialDomain, problem);
    }

    @Test
    public void parseFuelSequentialWithFuel() throws Exception {
        assertThat(variableDomainFuelSeq.getPddlLabels()).contains(PddlLabel.Fuel);
        VisualProblem problem = new VisualProblemIO(variableDomainFuelSeq).parse(fuelSeqProblemFileContents);
        TestUtils.assertP01Sequential(variableDomainFuelSeq, problem);
        assertThat(problem.getVisualRoadGraph().getNodeCount()).isEqualTo(5);
        assertThat(problem.getVisualRoadGraph().getAttributeKeySet().stream().anyMatch(a -> a.startsWith("ui.sprite")))
                .isTrue();
        assertThat(problem.getVisualRoadGraph().getAllLocations()).allMatch(l -> !l.hasPetrolStation());
        assertThat(problem.getRoadGraph().getAllRoads().map(RoadEdge::getRoad).map(Object::getClass))
                .allMatch(FuelRoad.class::equals);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getCurFuelCapacity() != null)
                .allMatch(v -> v.getMaxFuelCapacity() != null);
    }

    @Test
    public void parseSequentialWithFuel() throws Exception {
        VisualProblem problem = new VisualProblemIO(sequentialDomain).parse(fuelSeqProblemFileContents);
        TestUtils.assertP01Sequential(sequentialDomain, problem);
        assertThat(problem.getRoadGraph().getAllLocations().map(Location::getPetrolStation)).allMatch(Objects::isNull);
        assertThat(problem.getRoadGraph().getAllRoads().map(RoadEdge::getRoad).map(Object::getClass))
                .allMatch(DefaultRoad.class::equals);
        assertThat(problem.getAllVehicles()).allMatch(v -> v.getCurFuelCapacity() == null)
                .allMatch(v -> v.getMaxFuelCapacity() == null);
    }

    @Test
    public void parseTemporal() throws Exception {
        VisualProblem problem = new VisualProblemIO(variableDomainTemp).parse(tempProblemFileContents);
        assertNotNull(problem);

        RoadGraph rg = problem.getVisualRoadGraph();
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
    }

}
