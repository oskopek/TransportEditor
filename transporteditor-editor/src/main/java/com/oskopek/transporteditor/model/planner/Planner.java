package com.oskopek.transporteditor.model.planner;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;

public interface Planner {

    void startPlanning(Domain domain, Problem problem);

    void stopPlanning();

    Plan getBestPlan();

}
