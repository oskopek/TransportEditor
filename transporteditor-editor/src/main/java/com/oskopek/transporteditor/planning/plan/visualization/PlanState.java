/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan.visualization;

import com.oskopek.transporteditor.planning.domain.RoadGraph;
import com.oskopek.transporteditor.planning.problem.ActionObject;

import java.util.List;

/**
 * A specific snapshot of a plan. In the context of a specific plan, this describes
 *
 * Child/parent plan entries symbolize precomputed "dependency relations" between the current TimePoints
 * and the target preceding, concurrent or succeeding TimePoints.
 */
public interface PlanState {

    TimePoint getTimePoint();

    List<TimePoint> childTimePoints();

    List<TimePoint> parentTimePoints();

    List<ActionObject> getCurrentActionObjects();

    RoadGraph getCurrentGraph();

    /**
     * Returns the new state, after applying all needed changes to the current state so that we get to the point in the
     * plan specified by the new time point.
     *
     * @param newTimePoint
     * @return
     */
    PlanState applyDiffToState(TimePoint newTimePoint);

}
