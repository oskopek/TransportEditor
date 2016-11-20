package com.oskopek.transporteditor.persistence;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.TemporalQuantifier;
import com.oskopek.transporteditor.model.domain.action.functions.Capacity;
import com.oskopek.transporteditor.model.domain.action.functions.PackageSize;
import com.oskopek.transporteditor.model.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.model.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.model.problem.DefaultRoad;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import static com.oskopek.transporteditor.test.TestUtils.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;

public class VariableDomainIOIT {

    private static final String variableDomainSeqPDDL = "variableDomainSeq.pddl";
    private static final String variableDomainBPDDL = "variableDomainB.pddl";
    private static final String variableDomainTempPDDL = "variableDomainTemp.pddl";
    private static VariableDomainIO variableDomainIO;
    private static String variableDomainSeqPDDLContents;
    private static String variableDomainBPDDLContents;
    private static String variableDomainTempPDDLContents;
    private static RoadGraph roadGraph;
    private VariableDomain variableDomainSeq;
    private VariableDomain variableDomainB;

    @BeforeClass
    public static void setUpClass() throws Exception {
        variableDomainSeqPDDLContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainSeqPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainBPDDLContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainBPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainTempPDDLContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainTempPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainIO = new VariableDomainIO();

        roadGraph = new RoadGraph("");
        roadGraph.addLocation(new Location("a", 0, 0));
        roadGraph.addLocation(new Location("b", 0, 0));
        roadGraph.addRoad(new DefaultRoad("a->b", ActionCost.valueOf(11)), roadGraph.getLocation("a"),
                roadGraph.getLocation("b"));
    }

    @Before
    public void setUp() throws Exception {
        variableDomainSeq = spy(new VariableDomain("Transport sequential", null, null, null, null,
                ImmutableSet.of(PddlLabel.ActionCost, PddlLabel.Capacity, PddlLabel.MaxCapacity), null, null));
        when(variableDomainSeq.getFunctionMap()).thenReturn(
                ImmutableMap.of("road-length", RoadLength.class, "total-cost", TotalCost.class));
        when(variableDomainSeq.getPredicateMap()).thenReturn(
                ImmutableMap.of("at", WhoAtWhere.class, "capacity", HasCapacity.class, "in", In.class, "road",
                        IsRoad.class));

        variableDomainB = spy(new VariableDomain("Transport temporal without fuel", null, null, null, null,
                ImmutableSet.of(PddlLabel.Temporal, PddlLabel.Capacity, PddlLabel.MaxCapacity), null, null));
        when(variableDomainB.getFunctionMap()).thenReturn(ImmutableMap
                .of("capacity", Capacity.class, "package-size", PackageSize.class, "road-length", RoadLength.class));
        when(variableDomainB.getPredicateMap()).thenReturn(ImmutableMap
                .of("at", WhoAtWhere.class, "in", In.class, "road", IsRoad.class, "ready-loading", ReadyLoading.class));
    }

    @Test
    public void parseTemp() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainTempPDDLContents);
        assertNotNull(parsed);
        assertEquals(parsed, variableDomainSeq);

        assertContains(PddlLabel.Capacity, parsed.getPddlLabels());
        assertContains(PddlLabel.MaxCapacity, parsed.getPddlLabels());
        assertContains(PddlLabel.Temporal, parsed.getPddlLabels());
        assertNotContains(PddlLabel.ActionCost, parsed.getPddlLabels());
        assertContains(PddlLabel.Fuel, parsed.getPddlLabels());

        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(11),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"), roadGraph.getLocation("b"), roadGraph)
                        .getCost());
        assertEquals(ActionCost.valueOf(11),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"), roadGraph.getLocation("b"), roadGraph)
                        .getDuration());
        assertEquals(ActionCost.valueOf(10), parsed.getRefuelBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(10), parsed.getRefuelBuilder().build(null, null, null).getCost());


        // drive
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START),
                parsed.getDriveBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new IsRoad(), TemporalQuantifier.AT_START),
                parsed.getDriveBuilder().getPreconditions());
        assertEquals(2, parsed.getDriveBuilder().getPreconditions().size());

        assertContains(new TemporalPredicate(new Not(new WhoAtWhere()), TemporalQuantifier.AT_START),
                parsed.getDriveBuilder().getEffects());
        assertContains(new TemporalPredicate(new WhoAtWhat(), TemporalQuantifier.AT_END),
                parsed.getDriveBuilder().getEffects());
        assertEquals(2, parsed.getDriveBuilder().getEffects().size());

        // pickup
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START),
                parsed.getPickUpBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL),
                parsed.getPickUpBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_START),
                parsed.getPickUpBuilder().getPreconditions());
        assertEquals(3, parsed.getPickUpBuilder().getPreconditions().size());

        assertContains(new TemporalPredicate(new In(), TemporalQuantifier.AT_END),
                parsed.getPickUpBuilder().getEffects());
        assertContains(new TemporalPredicate(new Not(new WhatAtWhere()), TemporalQuantifier.AT_START),
                parsed.getPickUpBuilder().getEffects());
        assertEquals(2, parsed.getPickUpBuilder().getEffects().size());

        // drop
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START),
                parsed.getDropBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL),
                parsed.getDropBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new In(), TemporalQuantifier.AT_START),
                parsed.getDropBuilder().getPreconditions());
        assertEquals(3, parsed.getDropBuilder().getPreconditions().size());

        assertContains(new TemporalPredicate(new Not(new In()), TemporalQuantifier.AT_START),
                parsed.getDropBuilder().getEffects());
        assertContains(new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_END),
                parsed.getDropBuilder().getEffects());
        assertEquals(2, parsed.getDropBuilder().getEffects().size());

        assertNotNull(parsed.getRefuelBuilder());
    }

    @Test
    public void parseSeq() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(parsed, variableDomainSeq);

        assertContains(PddlLabel.Capacity, parsed.getPddlLabels());
        assertContains(PddlLabel.MaxCapacity, parsed.getPddlLabels());
        assertNotContains(PddlLabel.Temporal, parsed.getPddlLabels());
        assertContains(PddlLabel.ActionCost, parsed.getPddlLabels());
        assertNotContains(PddlLabel.Fuel, parsed.getPddlLabels());

        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(11),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"), roadGraph.getLocation("b"), roadGraph)
                        .getCost());
        assertEquals(ActionCost.valueOf(11),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"), roadGraph.getLocation("b"), roadGraph)
                        .getDuration());

        // drive
        assertContains(new WhoAtWhere(), parsed.getDriveBuilder().getPreconditions());
        assertContains(new IsRoad(), parsed.getDriveBuilder().getPreconditions());
        assertEquals(2, parsed.getDriveBuilder().getPreconditions().size());

        assertContains(new Not(new WhoAtWhere()), parsed.getDriveBuilder().getEffects());
        assertContains(new WhoAtWhat(), parsed.getDriveBuilder().getEffects());
        assertEquals(2, parsed.getDriveBuilder().getEffects().size());

        // pickup
        assertContains(new WhoAtWhere(), parsed.getPickUpBuilder().getPreconditions());
        assertContains(new WhatAtWhere(), parsed.getPickUpBuilder().getPreconditions());
        assertContains(new HasCapacity(), parsed.getPickUpBuilder().getPreconditions());
        assertEquals(3, parsed.getPickUpBuilder().getPreconditions().size());

        assertContains(new In(), parsed.getPickUpBuilder().getEffects());
        assertContains(new Not(new WhatAtWhere()), parsed.getPickUpBuilder().getEffects());
        assertEquals(2, parsed.getPickUpBuilder().getEffects().size());

        // drop
        assertContains(new WhoAtWhere(), parsed.getDropBuilder().getPreconditions());
        assertContains(new In(), parsed.getDropBuilder().getPreconditions());
        assertEquals(2, parsed.getDropBuilder().getPreconditions().size());

        assertContains(new Not(new In()), parsed.getDropBuilder().getEffects());
        assertContains(new WhatAtWhere(), parsed.getDropBuilder().getEffects());
        assertEquals(2, parsed.getDropBuilder().getEffects().size());

        assertNull(parsed.getRefuelBuilder());
        assertEquals(new SequentialDomain("Transport sequential"), parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSeqIllegalRoad() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(ActionCost.valueOf(0),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("b"), roadGraph.getLocation("a"), roadGraph)
                        .getDuration());
    }

    @Test
    public void serializeSeq() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainSeq);
        assertNotNull(serialized);
        assertEquals(variableDomainSeqPDDLContents, serialized);
    }

    @Test
    public void parseB() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainBPDDLContents);
        assertNotNull(parsed);

        assertContains(PddlLabel.Capacity, parsed.getPddlLabels());
        assertContains(PddlLabel.MaxCapacity, parsed.getPddlLabels());
        assertContains(PddlLabel.Temporal, parsed.getPddlLabels());
        assertNotContains(PddlLabel.ActionCost, parsed.getPddlLabels());
        assertNotContains(PddlLabel.Fuel, parsed.getPddlLabels());

        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertNotNull(parsed.getRefuelBuilder());

        // drive
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START),
                parsed.getDriveBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new IsRoad(), TemporalQuantifier.AT_START),
                parsed.getDriveBuilder().getPreconditions());
        assertEquals(2, parsed.getDriveBuilder().getPreconditions().size());

        assertContains(new TemporalPredicate(new Not(new WhoAtWhere()), TemporalQuantifier.AT_START),
                parsed.getDriveBuilder().getEffects());
        assertContains(new TemporalPredicate(new WhoAtWhat(), TemporalQuantifier.AT_END),
                parsed.getDriveBuilder().getEffects());
        assertEquals(2, parsed.getDriveBuilder().getEffects().size());

        // pickup
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START),
                parsed.getPickUpBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL),
                parsed.getPickUpBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_START),
                parsed.getPickUpBuilder().getPreconditions());
        assertEquals(3, parsed.getPickUpBuilder().getPreconditions().size());

        assertContains(new TemporalPredicate(new In(), TemporalQuantifier.AT_END),
                parsed.getPickUpBuilder().getEffects());
        assertContains(new TemporalPredicate(new Not(new WhatAtWhere()), TemporalQuantifier.AT_START),
                parsed.getPickUpBuilder().getEffects());
        assertEquals(2, parsed.getPickUpBuilder().getEffects().size());

        // drop
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START),
                parsed.getDropBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL),
                parsed.getDropBuilder().getPreconditions());
        assertContains(new TemporalPredicate(new In(), TemporalQuantifier.AT_START),
                parsed.getDropBuilder().getPreconditions());
        assertEquals(3, parsed.getDropBuilder().getPreconditions().size());

        assertContains(new TemporalPredicate(new Not(new In()), TemporalQuantifier.AT_START),
                parsed.getDropBuilder().getEffects());
        assertContains(new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_END),
                parsed.getDropBuilder().getEffects());
        assertEquals(2, parsed.getDropBuilder().getEffects().size());

        assertEquals(parsed, variableDomainB);
    }

    @Test
    public void serializeB() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainB);
        assertNotNull(serialized);
        assertEquals(variableDomainBPDDLContents, serialized);
    }

    public void deserializeSerialize(String fileName) throws Exception {
        String contents = readAllLines(VariableDomainIOIT.class.getResourceAsStream(fileName)).stream()
                .collect(Collectors.joining("\n")) + "\n";
        VariableDomain domain = variableDomainIO.parse(contents);
        assertNotNull(domain);
        String serialized = variableDomainIO.serialize(domain);
        assertEquals(contents, serialized);
    }

    @Test
    public void deserializeSerializeCompareSeq() throws Exception {
        deserializeSerialize(variableDomainSeqPDDL);
    }

    @Test
    public void deserializeSerializeCompareTemp() throws Exception {
        deserializeSerialize(variableDomainTempPDDL);
    }

    @Test
    public void deserializeSerializeCompareNum() throws Exception {
        deserializeSerialize("variableDomainNum.pddl");
    }

    @Test
    public void deserializeSerializeCompareB() throws Exception {
        deserializeSerialize(variableDomainBPDDL);
    }

}
