package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.DefaultPlanningSession;
import com.oskopek.transporteditor.planning.PlanningSession;
import com.thoughtworks.xstream.XStream;

/**
 * Created by t-onskop on 8/21/2016.
 */
public class DefaultPlanningSessionIO implements DataReader<DefaultPlanningSession>, DataWriter<PlanningSession> {

    private final XStream xStream;

    public DefaultPlanningSessionIO() {
        xStream = new XStream();
        xStream.setMode(XStream.ID_REFERENCES);
    }

    @Override
    public String serialize(PlanningSession object) throws IllegalArgumentException {
        return xStream.toXML(object);
    }

    @Override
    public DefaultPlanningSession parse(String contents) throws IllegalArgumentException {
        return (DefaultPlanningSession) xStream.fromXML(contents);
    }
}
