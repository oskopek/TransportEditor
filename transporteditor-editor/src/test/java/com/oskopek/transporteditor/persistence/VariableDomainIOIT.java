/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.oskopek.transporteditor.model.domain.DomainLabel;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.domain.action.functions.Capacity;
import com.oskopek.transporteditor.model.domain.action.functions.PackageSize;
import com.oskopek.transporteditor.model.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.model.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;

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
                Collectors.joining("\n"));
        variableDomainBPDDLContents = TestUtils.readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainBPDDL)).stream().collect(
                Collectors.joining("\n"));
        variableDomainIO = new VariableDomainIO();
    }

    @Before
    public void setUp() throws Exception {
        variableDomainSeq = spy(new VariableDomain("Transport sequential", null, null, null, null,
                ImmutableSet.of(DomainLabel.ActionCost, DomainLabel.Capacity, DomainLabel.MaxCapacity), null, null));
        when(variableDomainSeq.getFunctionMap()).thenReturn(
                ImmutableMap.of("road-length", RoadLength.class, "total-cost", TotalCost.class));
        when(variableDomainSeq.getPredicateMap()).thenReturn(
                ImmutableMap.of("at", At.class, "capacity", HasCapacity.class, "in", In.class, "road", IsRoad.class));

        variableDomainB = spy(new VariableDomain("Transport temporal without fuel", null, null, null, null,
                ImmutableSet.of(DomainLabel.Temporal, DomainLabel.Capacity, DomainLabel.MaxCapacity), null, null));
        when(variableDomainB.getFunctionMap()).thenReturn(ImmutableMap
                .of("capacity", Capacity.class, "package-size", PackageSize.class, "road-length", RoadLength.class));
        when(variableDomainB.getPredicateMap()).thenReturn(ImmutableMap
                .of("at", At.class, "in", In.class, "road", IsRoad.class, "ready-loading", ReadyLoading.class));
    }

    @Test
    public void parseSeq() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(parsed, variableDomainSeq);
        assertEquals(new SequentialDomain("Transport sequential"), parsed);
    }

    @Test
    public void serializeSeq() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainSeq);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(variableDomainSeqPDDLContents, serialized);
    }

    @Test
    @Ignore("Testing CI")
    public void parseB() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainBPDDLContents);
        assertNotNull(parsed);
        assertEquals(variableDomainB, parsed);
    }

    @Test
    public void serializeB() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainB);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(variableDomainBPDDLContents, serialized);
    }

    public void deserializeSerialize(String fileName) throws Exception {
        String contents = TestUtils.readAllLines(VariableDomainIOIT.class.getResourceAsStream(fileName)).stream()
                .collect(Collectors.joining("\n"));
        VariableDomain domain = variableDomainIO.parse(contents);
        assertNotNull(domain);
        String serialized = variableDomainIO.serialize(domain);
        TestUtils.assertPDDLContentEquals(contents, serialized);
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
