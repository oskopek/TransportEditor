package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.plan.PlanSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CompositeJsonPlanSessionReaderWriter implements DataReader<PlanSession>, DataWriter<PlanSession> {

    @Override
    public void writeTo(PlanSession planSession, String fileName) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public PlanSession readFrom(String fileName) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void writeTo(PlanSession planSession, OutputStream outputStream)
            throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public PlanSession readFrom(InputStream inputStream) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
