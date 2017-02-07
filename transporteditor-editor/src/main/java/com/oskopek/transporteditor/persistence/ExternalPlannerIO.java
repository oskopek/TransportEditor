package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;

public class ExternalPlannerIO extends XStreamGenericIO<ExternalPlanner> {

    @Override
    public ExternalPlanner parse(String contents) throws IllegalArgumentException {
        return parse(contents, ExternalPlanner.class);
    }
}
