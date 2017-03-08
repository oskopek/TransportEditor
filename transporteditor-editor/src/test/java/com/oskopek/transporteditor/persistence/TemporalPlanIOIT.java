package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.plan.TemporalPlan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.oskopek.transporteditor.persistence.IOUtils.readAllLines;
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
        assertEquals(2, plan.getActionsAt(1).size());
        assertEquals(2, plan.getActionsAt(45).size());
        assertEquals(1, plan.getActionsAt(46).size());
        assertEquals(2, plan.getActionsAt(47).size());
        assertEquals(1, plan.getActionsAt(48).size());
        assertEquals(0, plan.getActionsAt(51).size());
        assertEquals(1, plan.getActionsAt(52).size());
        assertEquals(0, plan.getActionsAt(53).size());
    }

}
