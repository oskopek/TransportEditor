/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.plan.TemporalPlan;
import com.oskopek.transporteditor.planning.problem.Problem;

public class TemporalPlanIO implements DataReader<TemporalPlan>, DataWriter<TemporalPlan> {

    private final Problem problem;

    public TemporalPlanIO(Problem problem) {
        this.problem = problem;
    }

    @Override
    public String serialize(TemporalPlan object) throws IllegalArgumentException {
        return null;
    }

    @Override
    public TemporalPlan parse(String contents) throws IllegalArgumentException {
        return null;
    }
}
