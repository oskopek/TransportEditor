/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.planstate;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.domain.RoadGraph;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.Problem;

import java.util.List;

public interface PlanState {

    PlanState buildFirst(Domain domain, Problem problem, Plan plan);

    List<PlanState> nextStates();

    List<PlanState> previousStates();

    List<ActionObject> getCurrentActionObjects();

    RoadGraph getCurrentGraph();


}
