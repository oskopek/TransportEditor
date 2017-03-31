package com.oskopek.transport.persistence;

import com.oskopek.transport.model.planner.ExternalPlanner;

/**
 * Specialized {@link XStreamGenericIO} for {@link ExternalPlanner}.
 */
public class ExternalPlannerIO extends XStreamGenericIO<ExternalPlanner> {

    @Override
    public ExternalPlanner parse(String contents) {
        return parse(contents, ExternalPlanner.class);
    }
}
