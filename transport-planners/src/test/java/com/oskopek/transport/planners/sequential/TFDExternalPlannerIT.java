package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.DefaultProblemIO;
import com.oskopek.transport.persistence.TemporalPlanIO;
import com.oskopek.transport.persistence.VariableDomainIO;
import com.oskopek.transport.planners.temporal.TFDExternalPlanner;
import com.oskopek.transport.tools.test.TestUtils;
import javaslang.control.Try;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.assumeTrue;

public class TFDExternalPlannerIT {

    private TFDExternalPlanner planner;
    private static Domain domain;
    private static Problem problem;
    private static Plan plan;

    @BeforeClass
    public static void setUpClass() throws Exception {
        domain = new VariableDomainIO().parse(TestUtils.getPersistenceTestFile("variableDomainTemp.pddl"));
        problem = new DefaultProblemIO(domain).parse(TestUtils.getPersistenceTestFile("p01TempProblem.pddl"));
        plan = new TemporalPlanIO(domain, problem).parse(TestUtils.getPersistenceTestFile("p01TempPlan.val"));
    }

    @Before
    public void setUp() throws Exception {
        planner = new TFDExternalPlanner();
    }

    @Test
    @Ignore("Temporal Fast Downward has to be installed.")
    public void isAvailable() throws Exception {
        assertThat(planner.isAvailable()).isTrue();
    }

    @Test
    public void plansP01Temporal() throws Exception {
        assumeTrue("Temporal Fast Downward planner is not available.", planner.isAvailable());
        CompletableFuture<Plan> plan = planner.startAsync(domain, problem).toCompletableFuture();
        Try.run(() -> Thread.sleep(5_000));
        assertThat(planner.cancel()).isTrue();
        Try.run(() -> Thread.sleep(5_000));
        assertThat(plan.isDone()).isTrue();
        Plan result = plan.get();
        assertThat(result).isNotNull().isEqualTo(TFDExternalPlannerIT.plan);
    }

    @Test
    public void plansWrongProblem() throws Exception {
        assumeTrue("Temporal Fast Downward planner is not available.", planner.isAvailable());
        problem = problem.removeVehicle("truck-1");
        problem = problem.removeVehicle("truck-2");
        CompletionStage<Plan> plan = planner.startAsync(domain, problem);
        Plan result = plan.toCompletableFuture().get(60, TimeUnit.SECONDS);
        assertThat(result).isNull();
    }

}
