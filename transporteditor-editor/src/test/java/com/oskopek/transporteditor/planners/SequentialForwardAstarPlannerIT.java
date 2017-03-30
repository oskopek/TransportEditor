package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import com.oskopek.transporteditor.model.problem.Vehicle;
import com.oskopek.transporteditor.persistence.DefaultProblemIO;
import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import javaslang.collection.Stream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class SequentialForwardAstarPlannerIT {

    private SequentialForwardAstarPlanner planner;
    private static final SequentialDomain domain = new SequentialDomain("");
    private static Problem problem;
    private static Problem p02Problem;
    private static Problem p03Problem;
    private static Plan plan;
    private static Plan planEquivalent;
    private static Plan p02Plan;

    @BeforeClass
    public static void setUpClass() throws Exception {
        problem = new DefaultProblemIO(domain).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p01SeqProblem.pddl")));
        p02Problem = new DefaultProblemIO(domain).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p02SeqProblem.pddl")));
        p03Problem = new DefaultProblemIO(domain).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p03SeqProblem.pddl")));
        plan = new SequentialPlanIO(domain, problem).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p01SeqPlan.val")));
        p02Plan = new SequentialPlanIO(domain, p02Problem).parse(IOUtils.concatReadAllLines(
                SequentialForwardBFSPlannerIT.class.getResourceAsStream("../persistence/p02SeqPlan.val")));

        List<Action> actions = new ArrayList<>(plan.getActions());
        Action action0 = actions.remove(0);
        actions.add(1, action0);
        planEquivalent = new SequentialPlan(actions);
    }

    @Before
    public void setUp() throws Exception {
        planner = new SequentialForwardAstarPlanner();
    }

    @Test
    public void isAvailable() throws Exception {
        assertThat(planner.isAvailable()).isTrue();
    }

    @Test
    public void plansP01Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, problem);
        SequentialForwardBFSPlannerIT
                .assertThatPlanIsEqualToAny(plan, SequentialForwardAstarPlannerIT.plan, planEquivalent);
    }

    @Test
    @Ignore
    public void plansP02Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, p02Problem);
        System.out.println(new SequentialPlanIO(domain, p02Problem).serialize(plan));

        assertThat(Stream.ofAll(plan.getTemporalPlanActions()).last().getEndTimestamp())
                .isEqualTo(Stream.ofAll(p02Plan.getTemporalPlanActions()).last().getEndTimestamp());
        SequentialForwardBFSPlannerIT.assertThatPlanIsEqualToAny(plan, p02Plan);
    }

    @Test
    @Ignore
    public void plansP03Sequential() throws Exception {
        Plan plan = planner.startAndWait(domain, p03Problem);
        System.out.println(new SequentialPlanIO(domain, p03Problem).serialize(plan));
//        aSequentialForwardBFSPlannerIT.assertThatPlanIsEqualToAny(plan, SequentialForwardAstarPlannerIT.plan,
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
        assertThat(SequentialForwardAstarPlanner
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
