package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.DefaultProblemIO;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.assumeTrue;

public class FastDownwardExternalPlannerIT {

    private FastDownwardExternalPlanner planner;
    private static final SequentialDomain domain = new SequentialDomain("");
    private static Problem problem;
    private static Plan plan;
    private static Plan planEquivalent;

    @BeforeClass
    public static void setUpClass() throws Exception {
        problem = new DefaultProblemIO(domain).parse(TestUtils.getPersistenceTestFile("p01SeqProblem.pddl"));
        plan = new SequentialPlanIO(domain, problem).parse(TestUtils.getPersistenceTestFile("p01SeqPlan.val"));

        List<Action> actions = new ArrayList<>(plan.getActions());
        Action action0 = actions.remove(0);
        actions.add(1, action0);
        planEquivalent = new SequentialPlan(actions);
    }

    @Before
    public void setUp() throws Exception {
        planner = new FastDownwardExternalPlanner();
    }

    @Test
    @Ignore("Fast Downward has to be installed.")
    public void isAvailable() throws Exception {
        assertThat(planner.isAvailable()).isTrue();
    }

    @Test
    public void plansP01Sequential() throws Exception {
        assumeTrue("Fast Downward planner is not available.", planner.isAvailable());
        final Plan plan = planner.startAndWait(domain, problem);
        assertThat(plan).isNotNull();
        if (!plan.equals(FastDownwardExternalPlannerIT.plan)) {
            assertThat(plan).isEqualTo(planEquivalent); // two valid plan variants
        }
    }

    @Test
    public void plansP01SequentialAlias() throws Exception {
        assumeTrue("Fast Downward planner is not available.", planner.isAvailable());
        planner = new FastDownwardExternalPlanner("{2} --alias seq-sat-lama-2011 {0} {1}");
        Plan plan = planner.startAndWait(domain, problem);
        assertThat(plan).isNotNull();
        if (!plan.equals(FastDownwardExternalPlannerIT.plan)) {
            assertThat(plan).isEqualTo(planEquivalent); // two valid plan variants
        }
    }

    @Test
    public void plansP01SequentialDifferentSearch() throws Exception {
        assumeTrue("Fast Downward planner is not available.", planner.isAvailable());
        planner = new FastDownwardExternalPlanner("{2} {0} {1} --search astar(ff())");
        Plan plan = planner.startAndWait(domain, problem);
        assertThat(plan).isNotNull();
        if (!plan.equals(FastDownwardExternalPlannerIT.plan)) {
            assertThat(plan).isEqualTo(planEquivalent); // two valid plan variants
        }
    }

}
