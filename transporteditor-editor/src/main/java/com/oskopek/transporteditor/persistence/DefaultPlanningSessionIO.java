package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.PlanningSession;

/**
 * Specialized {@link XStreamGenericIO} for {@link PlanningSession}.
 */
public class DefaultPlanningSessionIO extends XStreamGenericIO<PlanningSession> {

    @Override
    public PlanningSession parse(String contents) {
        return parse(contents, PlanningSession.class);
    }

}
