package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.DefaultProblemIO;
import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class SequentialForwardBFSPlannerIT {

    private SequentialForwardBFSPlanner planner;
    private static final SequentialDomain domain = new SequentialDomain("");
    private static Problem problem;
    private static Plan plan;
    private static Plan planEquivalent;

    @BeforeClass
    public static void setUpClass() throws Exception {
        problem = new DefaultProblemIO(domain).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p01SeqProblem.pddl")));
        plan = new SequentialPlanIO(domain, problem).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p01SeqPlan.val")));

        List<Action> actions = plan.getActions().stream().collect(Collectors.toList());
        Action action0 = actions.remove(0);
        actions.add(1, action0);
        planEquivalent = new SequentialPlan(actions);
    }

    @Before
    public void setUp() throws Exception {
        planner = new SequentialForwardBFSPlanner();
    }

    @Test
    public void isAvailable() throws Exception {
        assertThat(planner.isAvailable()).isTrue();
    }

    @Test
    public void plansP01Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, problem);
        if (!plan.equals(SequentialForwardBFSPlannerIT.plan)) {
            assertThat(plan).isEqualTo(planEquivalent); // two valid plan variants
        }
    }

}
