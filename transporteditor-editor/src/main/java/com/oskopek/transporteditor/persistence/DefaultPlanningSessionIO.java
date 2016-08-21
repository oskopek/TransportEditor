package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.DefaultPlanningSession;
import com.oskopek.transporteditor.planning.PlanningSession;

/**
 * Created by t-onskop on 8/21/2016.
 */
public class DefaultPlanningSessionIO implements DataReader<DefaultPlanningSession>, DataWriter<PlanningSession> {
    @Override
    public String serialize(PlanningSession object) throws IllegalArgumentException {
        return XStream.serialize(object);
    }

    @Override
    public DefaultPlanningSession parse(String contents) throws IllegalArgumentException {
        return XStream.parse(contents);
    }
}
