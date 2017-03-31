package com.oskopek.transport.session;

import com.oskopek.transport.persistence.XStreamGenericIO;

/**
 * Specialized {@link XStreamGenericIO} for {@link PlanningSession}.
 */
public class DefaultPlanningSessionIO extends XStreamGenericIO<PlanningSession> {

    @Override
    public PlanningSession parse(String contents) {
        return parse(contents, PlanningSession.class);
    }

}
