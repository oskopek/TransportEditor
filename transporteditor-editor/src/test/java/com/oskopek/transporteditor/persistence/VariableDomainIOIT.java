package com.oskopek.transporteditor.persistence;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.functions.Capacity;
import com.oskopek.transporteditor.model.domain.action.functions.PackageSize;
import com.oskopek.transporteditor.model.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.model.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class VariableDomainIOIT {

    private static final String variableDomainSeqPDDL = "variableDomainSeq.pddl";
    private static final String variableDomainBPDDL = "variableDomainB.pddl";
    private static VariableDomainIO variableDomainIO;
    private static String variableDomainSeqPDDLContents;
    private static String variableDomainBPDDLContents;
    private VariableDomain variableDomainSeq;
    private VariableDomain variableDomainB;

    @BeforeClass
    public static void setUpClass() throws Exception {
        variableDomainSeqPDDLContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainSeqPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainBPDDLContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainBPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainIO = new VariableDomainIO();
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
    public void parseSeq() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(parsed, variableDomainSeq);
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(1), parsed.getDriveBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getDriveBuilder().build(null, null, null).getDuration());
        assertEquals(null, parsed.getRefuelBuilder());
        assertEquals(new SequentialDomain("Transport sequential"), parsed);
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
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.valueOf(1), parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertEquals(null, parsed.getRefuelBuilder());
        assertEquals(parsed, variableDomainB);
    }

    @Test
    public void serializeB() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainB);
        assertNotNull(serialized);
        assertEquals(variableDomainBPDDLContents, serialized);
    }

    public void deserializeSerialize(String fileName) throws Exception {
        String contents = TestUtils.readAllLines(VariableDomainIOIT.class.getResourceAsStream(fileName)).stream()
                .collect(Collectors.joining("\n")) + "\n";
        VariableDomain domain = variableDomainIO.parse(contents);
        assertNotNull(domain);
        String serialized = variableDomainIO.serialize(domain);
        assertEquals(contents, serialized);
    }

    @Test
    public void deserializeSerializeCompareSeq() throws Exception {
        deserializeSerialize("variableDomainSeq.pddl");
    }

    @Test
    public void deserializeSerializeCompareTemp() throws Exception {
        deserializeSerialize("variableDomainTemp.pddl");
    }

    @Test
    public void deserializeSerializeCompareNum() throws Exception {
        deserializeSerialize("variableDomainNum.pddl");
    }

}
