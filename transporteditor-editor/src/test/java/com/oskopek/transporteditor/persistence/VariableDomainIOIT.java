/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.DomainType;
import com.oskopek.transporteditor.planning.domain.SequentialDomain;
import com.oskopek.transporteditor.planning.domain.VariableDomain;
import com.oskopek.transporteditor.planning.domain.action.functions.Capacity;
import com.oskopek.transporteditor.planning.domain.action.functions.PackageSize;
import com.oskopek.transporteditor.planning.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.planning.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.planning.domain.action.predicates.*;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
        variableDomainSeq = spy(new VariableDomain(DomainType.ActionCost, null, null));
        when(variableDomainSeq.getFunctionList()).thenReturn(Arrays.asList(RoadLength.class, TotalCost.class));
        when(variableDomainSeq.getPredicateList()).thenReturn(
                Arrays.asList(At.class, HasCapacity.class, In.class, IsRoad.class));

        variableDomainB = spy(new VariableDomain(DomainType.Temporal, null, null));
        when(variableDomainB.getFunctionList()).thenReturn(
                Arrays.asList(Capacity.class, PackageSize.class, RoadLength.class));
        when(variableDomainB.getPredicateList()).thenReturn(
                Arrays.asList(At.class, In.class, IsRoad.class, ReadyLoading.class));
    }

    @Test
    public void parseSeq() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(parsed, variableDomainSeq);
        assertEquals(new SequentialDomain(""), parsed);
    }

    @Test
    public void serializeSeq() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainSeq);
        assertNotNull(serialized);
        TestUtils.assertPDDLContentEquals(variableDomainSeqPDDLContents, serialized);
    }

    @Test
    public void parseB() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainBPDDLContents);
        assertNotNull(parsed);
        assertEquals(parsed, variableDomainB);
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
