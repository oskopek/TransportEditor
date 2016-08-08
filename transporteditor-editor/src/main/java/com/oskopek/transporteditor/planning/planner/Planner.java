package com.oskopek.transporteditor.planning.planner;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.Problem;

public interface Planner {

    void startPlanning(Domain domain, Problem problem);

    void stopPlanning();

    Plan getBestPlan();

}
