package com.oskopek.transporteditor.planners.sequential;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.plan.Plan;
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

public class PrologBFSExternalPlannerIT {

    private PrologBFSExternalPlanner planner;
    private static final SequentialDomain domain = new SequentialDomain("");
    private static Problem problem;
    private static Plan plan;

    @BeforeClass
    public static void setUpClass() throws Exception {
        problem = new DefaultProblemIO(domain).parse(IOUtils.concatReadAllLines(
                PrologBFSExternalPlannerIT.class.getResourceAsStream("../../persistence/p01SeqProblem.pddl")));
        plan = new SequentialPlanIO(domain, problem).parse(IOUtils.concatReadAllLines(
                PrologBFSExternalPlannerIT.class.getResourceAsStream("../../persistence/p01SeqPlan.val")));
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
