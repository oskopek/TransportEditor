package com.oskopek.transport.benchmark;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import javaslang.Function3;

/**
 * Type alias for plan score functions.
 */
public interface ScoreFunction extends Function3<Domain, Problem, Plan, Double> {

    // intentionally empty

}
