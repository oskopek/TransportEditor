package com.oskopek.transporteditor.planners.benchmark;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import javaslang.Function3;

/**
 * Type alias for plan score functions.
 */
public interface ScoreFunction extends Function3<Domain, Problem, Plan, Double> {

    // intentionally empty

}
