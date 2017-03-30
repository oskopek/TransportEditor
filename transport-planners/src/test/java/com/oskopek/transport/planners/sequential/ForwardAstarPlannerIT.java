package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.RoadGraph;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.persistence.DefaultProblemIO;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.tools.test.TestUtils;
import javaslang.collection.Stream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ForwardAstarPlannerIT { // TODO: Split into planner utils test and Astar IT

    private ForwardAstarPlanner planner;
    private static final SequentialDomain domain = new SequentialDomain("");
    private static Problem problem;
    private static Problem p02Problem;
    private static Problem p03Problem;
    private static Plan plan;
    private static Plan planEquivalent;
    private static Plan p02Plan;

    @BeforeClass
    public static void setUpClass() throws Exception {
        problem = new DefaultProblemIO(domain).parse(TestUtils.getPersistenceTestFile("p01SeqProblem.pddl"));
        p02Problem = new DefaultProblemIO(domain).parse(TestUtils.getPersistenceTestFile("p02SeqProblem.pddl"));
        p03Problem = new DefaultProblemIO(domain).parse(TestUtils.getPersistenceTestFile("p03SeqProblem.pddl"));
        plan = new SequentialPlanIO(domain, problem).parse(TestUtils.getPersistenceTestFile("p01SeqPlan.val"));
        p02Plan = new SequentialPlanIO(domain, p02Problem).parse(TestUtils.getPersistenceTestFile("p02SeqPlan.val"));

        List<Action> actions = new ArrayList<>(plan.getActions());
        Action action0 = actions.remove(0);
        actions.add(1, action0);
        planEquivalent = new SequentialPlan(actions);
    }

    @Before
    public void setUp() throws Exception {
        planner = new ForwardAstarPlanner();
    }

    @Test
    public void isAvailable() throws Exception {
        assertThat(planner.isAvailable()).isTrue();
    }

    @Test
    public void plansP01Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, problem);
        ForwardBFSPlannerIT
                .assertThatPlanIsEqualToAny(plan, ForwardAstarPlannerIT.plan, planEquivalent);
    }

    @Test
    @Ignore
    public void plansP02Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, p02Problem);
        System.out.println(new SequentialPlanIO(domain, p02Problem).serialize(plan));

        assertThat(Stream.ofAll(plan.getTemporalPlanActions()).last().getEndTimestamp())
                .isEqualTo(Stream.ofAll(p02Plan.getTemporalPlanActions()).last().getEndTimestamp());
        ForwardBFSPlannerIT.assertThatPlanIsEqualToAny(plan, p02Plan);
    }

    @Test
    @Ignore
    public void plansP03Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, p03Problem);
        System.out.println(new SequentialPlanIO(domain, p03Problem).serialize(plan));
//        aSequentialForwardBFSPlannerIT.assertThatPlanIsEqualToAny(plan, ForwardAstarPlannerIT.plan,
// planEquivalent);
    }

    @Test
    public void calculateHeuristic() throws Exception {
    }

    @Test
    public void hasCycle() throws Exception {
    }

    @Test
    public void getUnfinishedPackage() throws Exception {
    }

    @Test
    public void pickupWhereDropoff() throws Exception {
    }

    @Test
    public void doesShorterPathExist() throws Exception {
        planner.resetState();
        planner.initialize(domain, p02Problem);
        Vehicle truck1 = p02Problem.getVehicle("truck-1");
        Vehicle truck2 = p02Problem.getVehicle("truck-2");
        RoadGraph g = p02Problem.getRoadGraph();

        List<Action> planPart = new ArrayList<>();
        planPart.add(domain.buildDrive(truck1, g.getLocation("city-loc-6"), g.getLocation("city-loc-9"), g));
        planPart.add(domain.buildDrive(truck2, g.getLocation("city-loc-6"), g.getLocation("city-loc-1"), g));
        planPart.add(domain.buildDrive(truck1, g.getLocation("city-loc-9"), g.getLocation("city-loc-4"), g));
        assertThat(PlannerUtils
                .doesShorterPathExist(truck1, g.getLocation("city-loc-4"), planPart, planner.getDistanceMatrix()))
                .isTrue();
    }

    @Test
    public void calculateSumOfDistancesToPackageTargets() throws Exception {
    }

    @Test
    public void getLengthToCorrect() throws Exception {
    }

}
