package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.*;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.oskopek.transport.persistence.IOUtils.concatReadAllLines;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SequentialPlanIOIT {

    private static final SequentialDomain domain = new SequentialDomain("Transport sequential");
    public static final Problem p01Problem = TestUtils.P01SequentialProblem();
    public static Problem p20Problem;
    public static String P01SequentialPlanFileContents;
    public static String P20SequentialPlanFileContents;

    @BeforeClass
    public static void setUpClass() throws Exception {
        initialize();
    }

    /**
     * Used by other classes as well.
     *
     * @throws IOException if an error occurs during plan/problem reading
     */
    public static void initialize() throws IOException {
        p20Problem = new DefaultProblemIO(domain).parse(concatReadAllLines(SequentialPlanIOIT.class
                .getResourceAsStream("p20SeqProblem.pddl")));
        P01SequentialPlanFileContents = concatReadAllLines(SequentialPlanIOIT.class
                .getResourceAsStream("p01SeqPlan.val"));
        P20SequentialPlanFileContents = concatReadAllLines(SequentialPlanIOIT.class
                .getResourceAsStream("p20SeqPlan.val"));
    }


    @Test
    public void serialize() throws Exception {
        SequentialPlan P01SequentialPlan = TestUtils.P01SequentialPlan();
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(domain, p01Problem);
        String serializedPlan = sequentialPlanIO.serialize(P01SequentialPlan);
        assertNotNull(serializedPlan);
        assertEquals(P01SequentialPlanFileContents, serializedPlan);
    }

    @Test
    public void parse() throws Exception {
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(domain, p01Problem);
        SequentialPlan plan = sequentialPlanIO.parse(P01SequentialPlanFileContents);
        assertNotNull(plan);
        assertEquals(plan, TestUtils.P01SequentialPlan());
    }

    @Test
    public void parseSerializeLarge() throws Exception {
        SequentialPlanIO sequentialPlanIO = new SequentialPlanIO(domain, p20Problem);
        SequentialPlan plan = sequentialPlanIO.parse(P20SequentialPlanFileContents);
        assertNotNull(plan);
        TestUtils.assertPDDLContentEquals(P20SequentialPlanFileContents, sequentialPlanIO.serialize(plan));
    }
}
