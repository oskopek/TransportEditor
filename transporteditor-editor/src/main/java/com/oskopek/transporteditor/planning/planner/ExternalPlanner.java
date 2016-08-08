package com.oskopek.transporteditor.planning.planner;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.Problem;

public class ExternalPlanner implements Planner {

    @Override
    public void startPlanning(Domain domain, Problem problem) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void stopPlanning() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Plan getBestPlan() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
