/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan.visualization;

import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.RoadGraph;

import java.util.List;
import java.util.Map;

public class DefaultPlanState implements PlanState {

    @Override
    public TimePoint getTimePoint() {
        return null;
    }

    @Override
    public List<TimePoint> childTimePoints() {
        return null;
    }

    @Override
    public List<TimePoint> parentTimePoints() {
        return null;
    }

    @Override
    public Map<String, ActionObject> getCurrentActionObjects() {
        return null;
    }

    @Override
    public RoadGraph getCurrentGraph() {
        return null;
    }

    @Override
    public PlanState applyDiffToState(TimePoint newTimePoint) {
        return null;
    }
}
