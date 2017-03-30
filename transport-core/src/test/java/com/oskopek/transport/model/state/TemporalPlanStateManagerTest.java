package com.oskopek.transport.model.state;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.persistence.SequentialPlanIOIT;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class TemporalPlanStateManagerTest {

    private static final Domain domain = new SequentialDomain("Transport sequential");
    private static final Problem problem = SequentialPlanIOIT.P01SequentialProblem();
    private static final Plan plan = SequentialPlanIOIT.P01SequentialPlan(problem);
    private static final List<TemporalPlanAction> actions = new ArrayList<>(plan.getTemporalPlanActions());

    private TemporalPlanStateManager planStateManager;
    private PlanState planState;

    @Before
    public void setUp() throws Exception {
        planStateManager = new TemporalPlanStateManager(domain, problem, plan);
        planState = new DefaultPlanState(domain, problem);
    }

    @Test
    public void getCurrentPlanState() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
    }

    @Test
    public void getCurrentTime() throws Exception {
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
        planStateManager.goToTime(1d, false);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(1d);
    }

    @Test
    public void getCurrentTimeRightAfter() throws Exception {
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
        planStateManager.goToTime(1d, true);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(1d);
    }

    @Test
    public void goToTimeZero() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToTime(0d, false);

        assertThat(planStateManager.getLastAction()).isEmpty();
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
    }

    @Test
    public void goToTimeZeroWithStarts() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        Package pkg = problem.getPackage("package-1").updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateReadyLoading(false);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg.getName(), pkg);

        planStateManager.goToTime(0d, true);

        assertThat(planStateManager.getLastAction()).hasValue(actions.get(0));
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
    }

    @Test
    public void goToTimeOne() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToTime(1d, false);

        assertThat(planStateManager.getLastAction()).hasValue(actions.get(0));

        Package pkg = problem.getPackage("package-1").updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateReadyLoading(true).addPackage(pkg);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg.getName(), pkg);

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(1d);
    }

    @Test
    public void goToTimeOneWithStarts() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        Package pkg1 = problem.getPackage("package-1").updateLocation(null);
        Package pkg2 = problem.getPackage("package-2").updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateReadyLoading(false).addPackage(pkg1);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg1.getName(), pkg1)
                .putPackage(pkg2.getName(), pkg2);

        planStateManager.goToTime(1d, true);

        assertThat(planStateManager.getLastAction()).hasValue(actions.get(1));

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(1d);
    }

    @Test
    public void goToTimeWholePlan() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToTime(54d, false);
        assertThat(planStateManager.getLastAction()).hasValue(actions.get(5));

        Package pkg1 = problem.getPackage("package-1");
        pkg1 = pkg1.updateLocation(pkg1.getTarget());
        Package pkg2 = problem.getPackage("package-2");
        pkg2 = pkg2.updateLocation(pkg2.getTarget());
        Vehicle truck = problem.getVehicle("truck-1").updateLocation(pkg2.getTarget());
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg1.getName(), pkg1)
                .putPackage(pkg2.getName(), pkg2);

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(54d);
    }

    @Test
    public void goToNextCheckpointWholePlan() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        for (int i = 0; i < 50; i++) {
            planStateManager.goToNextCheckpoint();
        }

        Package pkg1 = problem.getPackage("package-1");
        pkg1 = pkg1.updateLocation(pkg1.getTarget());
        Package pkg2 = problem.getPackage("package-2");
        pkg2 = pkg2.updateLocation(pkg2.getTarget());
        Vehicle truck = problem.getVehicle("truck-1").updateLocation(pkg2.getTarget());
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg1.getName(), pkg1)
                .putPackage(pkg2.getName(), pkg2);

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(54d);
    }

    @Test
    public void goToNextAndPreviousCheckpointOne() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToNextCheckpoint();
        assertThat(planStateManager.getLastAction()).hasValue(actions.get(0));
        planStateManager.goToPreviousCheckpoint();
        assertThat(planStateManager.getLastAction()).isEmpty();

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
    }

    @Test
    public void goToNextAndPreviousCheckpoint() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        final int n = 5;
        for (int i = 0; i < n; i++) {
            planStateManager.goToNextCheckpoint();
            assertThat(planStateManager.getLastAction()).hasValue(actions.get(i));
        }
        for (int i = 0; i < n; i++) {
            assertThat(planStateManager.getLastAction()).hasValue(actions.get(n - i - 1));
            planStateManager.goToPreviousCheckpoint();
        }
        assertThat(planStateManager.getLastAction()).isEmpty();
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
    }

    @Test
    public void goToPreviousCheckpointWholePlan() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        for (int i = 0; i < 50; i++) {
            planStateManager.goToNextCheckpoint();
        }
        planStateManager.goToPreviousCheckpoint();

        Package pkg1 = problem.getPackage("package-1");
        pkg1 = pkg1.updateLocation(pkg1.getTarget());
        Package pkg2 = problem.getPackage("package-2");
        pkg2 = pkg2.updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateLocation(pkg2.getTarget()).addPackage(pkg2);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg1.getName(), pkg1)
                .putPackage(pkg2.getName(), pkg2);

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(53d);
    }

    @Test
    public void goToPreviousCheckpointWholePlanAndBack() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        final int n = 50;
        for (int i = 0; i < n; i++) {
            planStateManager.goToNextCheckpoint();
        }
        for (int i = 0; i < n; i++) {
            planStateManager.goToPreviousCheckpoint();
        }
        assertThat(planStateManager.getLastAction()).isEmpty();
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);
    }

    @Test
    public void goToInProgressAndLastActionTest() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToTime(25d, false);
        assertThat(planStateManager.getLastAction()).isNotEmpty().hasValue(actions.get(2)); // drive is in progress
        assertThat(planStateManager.getCurrentTime()).isEqualTo(25d);
    }

    @Test
    public void goToBeginningWithoutApplyStartsAndLastActionTest() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToTime(2d, false);
        // pickup ended, drive didn't begin yet
        assertThat(planStateManager.getLastAction()).isNotEmpty().hasValue(actions.get(1));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(2d);
    }

    @Test
    public void goToBeginningWithApplyStartsAndLastActionTest() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(0d);

        planStateManager.goToTime(2d, true);
        assertThat(planStateManager.getLastAction()).isNotEmpty().hasValue(actions.get(2)); // drive has started
        assertThat(planStateManager.getCurrentTime()).isEqualTo(2d);
    }

}
