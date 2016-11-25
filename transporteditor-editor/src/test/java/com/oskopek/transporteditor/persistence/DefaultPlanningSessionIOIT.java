package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.DefaultPlanningSession;
import org.junit.Before;
import org.junit.Test;

import static com.oskopek.transporteditor.test.TestUtils.readAllConcatenatedLines;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        DefaultPlanningSession parsed = defaultPlanningSessionIO.parse(emptySessionFileContents);
        testEqualityGradually(new DefaultPlanningSession(), parsed);
    }

    @Test
    public void testP01SessionEquality() {
        DefaultPlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        testEqualityGradually(referenceSession, parsed);
    }

    private void testEqualityGradually(DefaultPlanningSession referenceSession, DefaultPlanningSession parsed) {
        assertNotNull(parsed);
        assertNull(parsed.getPlanner());
        assertNull(parsed.getValidator());
        assertEquals(referenceSession.getDomain(), parsed.getDomain());
        assertEquals(referenceSession.getProblem(), parsed.getProblem());
        assertEquals(referenceSession.getPlan(), parsed.getPlan());
        assertEquals(referenceSession, parsed);
    }

}
