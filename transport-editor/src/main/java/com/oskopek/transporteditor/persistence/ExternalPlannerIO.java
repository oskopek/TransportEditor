package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;

/**
 * Specialized {@link XStreamGenericIO} for {@link ExternalPlanner}.
 */
public class ExternalPlannerIO extends XStreamGenericIO<ExternalPlanner> {

    @Override
    public ExternalPlanner parse(String contents) {
        return parse(contents, ExternalPlanner.class);
    }
}
