package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.DefaultPlanningSession;
import com.oskopek.transporteditor.model.PlanningSession;
import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.validation.EmptyValidator;
import org.junit.Before;
import org.junit.Test;

import static com.oskopek.transporteditor.test.TestUtils.readAllConcatenatedLines;
import static org.junit.Assert.*;

public class DefaultPlanningSessionIOIT {

    private static DefaultPlanningSessionIO defaultPlanningSessionIO;
    private static String emptySessionFileContents;
    private static String p01SessionContents;
    private static String domainFileContents;
    private static String problemFileContents;
    private static String planFileContents;
    private DefaultPlanningSession referenceSession;

    @Before
    public void setUp() throws Exception {
        emptySessionFileContents = readAllConcatenatedLines(
                getClass().getResourceAsStream("emptyDefaultPlanningSession.xml"));
        p01SessionContents = readAllConcatenatedLines(getClass().getResourceAsStream("p01DefaultPlanningSession.xml"));
        domainFileContents = readAllConcatenatedLines(getClass().getResourceAsStream("variableDomainSeq.pddl"));
        problemFileContents = readAllConcatenatedLines(getClass().getResourceAsStream("p01SeqProblem.pddl"));
        planFileContents = readAllConcatenatedLines(getClass().getResourceAsStream("p01SeqPlan.val"));
        defaultPlanningSessionIO = new DefaultPlanningSessionIO();
        referenceSession = new DefaultPlanningSession();
        referenceSession.setDomain(new VariableDomainIO().parse(domainFileContents));
        referenceSession.setProblem(new DefaultProblemIO(referenceSession.getDomain()).parse(problemFileContents));
        referenceSession.setPlan(new SequentialPlanIO(referenceSession.getDomain(), referenceSession.getProblem())
                .parse(planFileContents));
    }

    @Test
    public void testEmptySessionEquality() {
        PlanningSession parsed = defaultPlanningSessionIO.parse(emptySessionFileContents);
        testEqualityGradually(new DefaultPlanningSession(), parsed);
    }

    @Test
    public void testP01SessionEquality() {
        PlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        testEqualityGradually(referenceSession, parsed);
    }

    @Test
    public void testP01SessionSerializeEquality() {
        PlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        testEqualityGradually(referenceSession, parsed);
        String serialized = defaultPlanningSessionIO.serialize(parsed);
        PlanningSession parsed2 = defaultPlanningSessionIO.parse(serialized);
        assertFalse(parsed == parsed2);
        testEqualityGradually(referenceSession, parsed2);
        testEqualityGradually(parsed, parsed2);
    }

    @Test
    public void testP01SessionInDepth() throws Exception {
        PlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        Domain domain = parsed.getDomain();
        VariableDomainIOIT.setUpClass();
        VariableDomainIOIT.assertSequentialDomain(domain);
        Problem problem = parsed.getProblem();
        DefaultProblemIOIT.assertP01Sequential(problem);
        Plan plan = parsed.getPlan();
        assertEquals(plan, SequentialPlanIOIT.P01SequentialPlan(problem));
    }

    @Test
    public void testP01SessionSerializeInDepth() throws Exception {
        PlanningSession first = defaultPlanningSessionIO.parse(p01SessionContents);
        String serialized = defaultPlanningSessionIO.serialize(first);
        PlanningSession parsed = defaultPlanningSessionIO.parse(serialized);
        Domain domain = parsed.getDomain();
        VariableDomainIOIT.setUpClass();
        VariableDomainIOIT.assertSequentialDomain(domain);
        Problem problem = parsed.getProblem();
        DefaultProblemIOIT.assertP01Sequential(problem);
        Plan plan = parsed.getPlan();
        assertEquals(plan, SequentialPlanIOIT.P01SequentialPlan(problem));
    }

    private void testEqualityGradually(PlanningSession referenceSession, PlanningSession parsed) {
        assertNotNull(parsed);
        assertNull(parsed.getPlanner());
        assertEquals(new EmptyValidator(), parsed.getValidator());
        assertEquals(referenceSession.getDomain(), parsed.getDomain());
        assertEquals(referenceSession.getProblem(), parsed.getProblem());
        assertEquals(referenceSession.getPlan(), parsed.getPlan());
        assertEquals(referenceSession, parsed);
    }

}
