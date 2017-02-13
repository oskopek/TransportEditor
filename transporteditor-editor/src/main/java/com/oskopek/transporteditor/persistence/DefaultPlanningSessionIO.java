package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.PlanningSession;

public class DefaultPlanningSessionIO extends XStreamGenericIO<PlanningSession> {

    @Override
    public PlanningSession parse(String contents) throws IllegalArgumentException {
        return parse(contents, PlanningSession.class);
    }

}
