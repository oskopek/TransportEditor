package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.DefaultProblemIO;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.assumeTrue;

public class PrologBFSExternalPlannerIT {

    private PrologBFSExternalPlanner planner;
    private static final SequentialDomain domain = new SequentialDomain("");
    private static Problem problem;
    private static Plan plan;

    @BeforeClass
    public static void setUpClass() throws Exception {
        problem = new DefaultProblemIO(domain).parse(TestUtils.getPersistenceTestFile("p01SeqProblem.pddl"));
        plan = new SequentialPlanIO(domain, problem).parse(TestUtils.getPersistenceTestFile("p01SeqPlan.val"));
    }

    @Before
    public void setUp() throws Exception {
        planner = new PrologBFSExternalPlanner();
    }

    @Test
    @Ignore("SWIPL has to be installed.")
    public void isAvailable() throws Exception {
        assertThat(planner.isAvailable()).isTrue();
    }

    @Test
    public void plansP01Sequential() throws Exception {
        assumeTrue("SWIPL has to be installed.", planner.isAvailable());
        Plan plan = planner.startAndWait(domain, problem);
        assertThat(plan).isNotNull().isEqualTo(PrologBFSExternalPlannerIT.plan);
    }

}
