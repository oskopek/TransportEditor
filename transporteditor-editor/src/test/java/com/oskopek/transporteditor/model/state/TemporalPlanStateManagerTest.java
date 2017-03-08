package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;
import com.oskopek.transporteditor.persistence.SequentialPlanIOIT;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
        planStateManager.goToTime(ActionCost.valueOf(1), false);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(1));
    }

    @Test
    public void getCurrentTimeRightAfter() throws Exception {
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
        planStateManager.goToTime(ActionCost.valueOf(1), true);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(1));
    }

    @Test
    public void goToTimeZero() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        planStateManager.goToTime(ActionCost.valueOf(0), false);

        assertThat(planStateManager.getLastAction()).isEmpty();
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
    }

    @Test
    public void goToTimeZeroWithStarts() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        Package pkg = problem.getPackage("package-1").updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateReadyLoading(false);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg.getName(), pkg);

        planStateManager.goToTime(ActionCost.valueOf(0), true);

        assertThat(planStateManager.getLastAction()).hasValue(actions.get(0));
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
    }

    @Test
    public void goToTimeOne() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        planStateManager.goToTime(ActionCost.valueOf(1), false);

        assertThat(planStateManager.getLastAction()).hasValue(actions.get(0));

        Package pkg = problem.getPackage("package-1").updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateReadyLoading(true).addPackage(pkg);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg.getName(), pkg);

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(1));
    }

    @Test
    public void goToTimeOneWithStarts() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        Package pkg1 = problem.getPackage("package-1").updateLocation(null);
        Package pkg2 = problem.getPackage("package-2").updateLocation(null);
        Vehicle truck = problem.getVehicle("truck-1").updateReadyLoading(false).addPackage(pkg1);
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg1.getName(), pkg1)
                .putPackage(pkg2.getName(), pkg2);

        planStateManager.goToTime(ActionCost.valueOf(1), true);

        assertThat(planStateManager.getLastAction()).hasValue(actions.get(1));

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(1));
    }

    @Test
    public void goToTimeWholePlan() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        planStateManager.goToTime(ActionCost.valueOf(54), false);
        assertThat(planStateManager.getLastAction()).hasValue(actions.get(5));


        Package pkg1 = problem.getPackage("package-1");
        pkg1 = pkg1.updateLocation(pkg1.getTarget());
        Package pkg2 = problem.getPackage("package-2");
        pkg2 = pkg2.updateLocation(pkg2.getTarget());
        Vehicle truck = problem.getVehicle("truck-1").updateLocation(pkg2.getTarget());
        Problem newProblem = problem.putVehicle(truck.getName(), truck).putPackage(pkg1.getName(), pkg1)
                .putPackage(pkg2.getName(), pkg2);

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(new DefaultPlanState(domain, newProblem));
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(54));
    }

    @Test
    public void goToNextCheckpointWholePlan() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

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
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(54));
    }

    @Test
    public void goToNextAndPreviousCheckpointOne() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        planStateManager.goToNextCheckpoint();
        assertThat(planStateManager.getLastAction()).hasValue(actions.get(0));
        planStateManager.goToPreviousCheckpoint();
        assertThat(planStateManager.getLastAction()).isEmpty();

        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
    }

    @Test
    public void goToNextAndPreviousCheckpoint() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

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
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
    }

    @Test
    public void goToPreviousCheckpointWholePlan() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

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
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(53));
    }

    @Test
    public void goToPreviousCheckpointWholePlanAndBack() throws Exception {
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));

        final int n = 50;
        for (int i = 0; i < n; i++) {
            planStateManager.goToNextCheckpoint();
        }
        for (int i = 0; i < n; i++) {
            planStateManager.goToPreviousCheckpoint();
        }
        assertThat(planStateManager.getLastAction()).isEmpty();
        assertThat(planStateManager.getCurrentPlanState()).isEqualTo(planState);
        assertThat(planStateManager.getCurrentTime()).isEqualTo(ActionCost.valueOf(0));
    }

}
