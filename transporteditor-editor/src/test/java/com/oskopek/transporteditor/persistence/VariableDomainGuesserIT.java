/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.SequentialDomain;
import com.oskopek.transporteditor.planning.domain.VariableDomain;
import com.oskopek.transporteditor.planning.domain.action.functions.Capacity;
import com.oskopek.transporteditor.planning.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.planning.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.planning.domain.action.predicates.*;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VariableDomainGuesserIT {

    private static final String variableDomainSeqPDDL = "variableDomainSeq.pddl";
    private static final String variableDomainBPDDL = "variableDomainB.pddl";
    private static VariableDomainGuesser variableDomainGuesser;
    private static String variableDomainSeqPDDLContents;
    private static String variableDomainBPDDLContents;
    private VariableDomain variableDomainSeq;
    private VariableDomain variableDomainB;

    @BeforeClass
    public static void setUpClass() throws Exception {
        variableDomainSeqPDDLContents = TestUtils.readAllLines(
                VariableDomainGuesserIT.class.getResourceAsStream(variableDomainSeqPDDL)).stream().collect(
                Collectors.joining(""));
        variableDomainBPDDLContents = TestUtils.readAllLines(
                VariableDomainGuesserIT.class.getResourceAsStream(variableDomainBPDDL)).stream().collect(
                Collectors.joining(""));
        variableDomainGuesser = new VariableDomainGuesser();
    }

    @Before
    public void setUp() throws Exception {
        variableDomainSeq = spy(new VariableDomain());
        when(variableDomainSeq.getFunctions()).thenReturn(Arrays.asList(new RoadLength(), new TotalCost()));
        when(variableDomainSeq.getPredicates()).thenReturn(
                Arrays.asList(new IsRoad(null), new At(null), new In(null), new HasCapacity(null)));

        variableDomainB = spy(new VariableDomain());
        when(variableDomainB.getFunctions()).thenReturn(
                Arrays.asList(new RoadLength(), new TotalCost(), new Capacity()));
        when(variableDomainB.getPredicates()).thenReturn(
                Arrays.asList(new IsRoad(TemporalQuantifier.AT_START), new At(TemporalQuantifier.AT_START),
                        new At(TemporalQuantifier.OVER_ALL), new At(TemporalQuantifier.AT_END),
                        new In(TemporalQuantifier.AT_START), new In(TemporalQuantifier.AT_END)));
    }

    @Test
    @Ignore("Not implemented yet")
    public void parseSeq() throws Exception {
        VariableDomain parsed = variableDomainGuesser.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(variableDomainSeq, parsed);
        assertEquals(new SequentialDomain(), parsed);
    }

    @Test
    @Ignore("Not implemented yet")
    public void serializeSeq() throws Exception {
        String serialized = variableDomainGuesser.serialize(variableDomainSeq);
        assertNotNull(serialized);
        assertEquals(variableDomainSeqPDDLContents, serialized);
    }

    @Test
    @Ignore("Not implemented yet")
    public void parseB() throws Exception {
        VariableDomain parsed = variableDomainGuesser.parse(variableDomainBPDDLContents);
        assertNotNull(parsed);
        assertEquals(variableDomainB, parsed);
    }

    @Test
    @Ignore("Not implemented yet")
    public void serializeB() throws Exception {
        String serialized = variableDomainGuesser.serialize(variableDomainB);
        assertNotNull(serialized);
        assertEquals(variableDomainBPDDLContents, serialized);
    }

}
