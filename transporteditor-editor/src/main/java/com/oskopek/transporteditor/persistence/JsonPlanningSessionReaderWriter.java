package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.plan.PlanningSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A composite {@link PlanningSession} {@link DataReader} and {@link DataWriter}, using other readers and writers
 * from parameters for persisting the child plan and domain.
 */
public class JsonPlanningSessionReaderWriter implements DataReader<PlanningSession>, DataWriter<PlanningSession> {

    @Override
    public void writeTo(PlanningSession planningSession, String fileName) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public PlanningSession readFrom(String fileName) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void writeTo(PlanningSession planningSession, OutputStream outputStream)
            throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public PlanningSession readFrom(InputStream inputStream) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
