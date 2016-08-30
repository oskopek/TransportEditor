/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.plan.SequentialPlan;
import com.oskopek.transporteditor.planning.problem.Problem;

public class SequentialPlanIO implements DataReader<SequentialPlan>, DataWriter<SequentialPlan> {

    private final Problem problem;

    public SequentialPlanIO(Problem problem) {
        this.problem = problem;
    }

    @Override
    public String serialize(SequentialPlan object) throws IllegalArgumentException {
        return null;
    }

    @Override
    public SequentialPlan parse(String contents) throws IllegalArgumentException {
        return null;
    }
}
