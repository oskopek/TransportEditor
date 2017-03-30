package com.oskopek.transport.persistence;

import com.oskopek.transport.model.PlanningSession;

/**
 * Specialized {@link XStreamGenericIO} for {@link PlanningSession}.
 */
public class DefaultPlanningSessionIO extends XStreamGenericIO<PlanningSession> {

    @Override
    public PlanningSession parse(String contents) {
        return parse(contents, PlanningSession.class);
    }

}
