package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.VariableDomain;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.domain.action.Drop;
import com.oskopek.transport.model.domain.action.PickUp;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.problem.DefaultProblem;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.oskopek.transport.persistence.IOUtils.readAllLines;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TemporalPlanIOIT {

    private static VariableDomain temporalDomain;
    private static DefaultProblem p01Temporal;
    private String p01TemporalPlanContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        temporalDomain = new VariableDomainIO().parse(readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("variableDomainTemp.pddl")).stream()
                .collect(Collectors.joining("\n")));
        p01Temporal = new DefaultProblemIO(temporalDomain).parse(readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01TempProblem.pddl")).stream()
                .collect(Collectors.joining("\n")));
    }

    @Before
    public void setUp() throws Exception {
        p01TemporalPlanContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream("p01TempPlan.val")).stream().collect(
                Collectors.joining("\n"));
    }

    @Test
    public void serialize() throws Exception {
        TemporalPlan plan = new TemporalPlanIO(temporalDomain, p01Temporal).parse(p01TemporalPlanContents);
        String serialized = new TemporalPlanIO(temporalDomain, p01Temporal).serialize(plan);
        TestUtils.assertPDDLContentEquals(p01TemporalPlanContents, serialized);
    }

    @Test
    public void parse() throws Exception {
        TemporalPlan plan = new TemporalPlanIO(temporalDomain, p01Temporal).parse(p01TemporalPlanContents);
        assertNotNull(plan);
        assertEquals(6, plan.getActions().size());
        assertEquals(2, plan.getActionsAt(0).size());
        assertThat(plan.getActionsAt(0.99)).allMatch(p -> p instanceof PickUp).hasSize(2);
        assertEquals(2, plan.getActionsAt(0.999).size());
        assertEquals(0, plan.getActionsAt(1).size());
        assertEquals(2, plan.getActionsAt(1.001).size());
        assertEquals(2, plan.getActionsAt(45).size());
        assertEquals(2, plan.getActionsAt(46).size());
        assertThat(plan.getActionsAt(46)).allMatch(p -> p instanceof Drive);
        assertEquals(2, plan.getActionsAt(47).size());
        assertEquals(1, plan.getActionsAt(48).size());
        assertEquals(1, plan.getActionsAt(51).size());
        assertEquals(1, plan.getActionsAt(51.01).size());
        assertThat(plan.getActionsAt(51)).allMatch(p -> p instanceof Drive);
        assertThat(plan.getActionsAt(51.000001)).allMatch(p -> p instanceof Drive);
        assertThat(plan.getActionsAt(51.00001)).allMatch(p -> p instanceof Drop);
        assertThat(plan.getActionsAt(51.001)).allMatch(p -> p instanceof Drop);
        assertEquals(1, plan.getActionsAt(52).size());
        assertEquals(0, plan.getActionsAt(53).size());
    }

}
