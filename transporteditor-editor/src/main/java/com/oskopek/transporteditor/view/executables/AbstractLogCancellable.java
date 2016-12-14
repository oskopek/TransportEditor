package com.oskopek.transporteditor.view.executables;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AbstractLogCancellable extends AbstractLogStreamable implements Cancellable {

    private final transient BooleanProperty killActiveProcess = new SimpleBooleanProperty(false);

    protected boolean getKillActiveProcess() {
        synchronized (this.killActiveProcess) {
            return killActiveProcess.get();
        }
    }

    protected void setKillActiveProcess(boolean killActiveProcess) {
        synchronized (this.killActiveProcess) {
            this.killActiveProcess.setValue(killActiveProcess);
        }
    }

    @Override
    public boolean cancel() {
        setKillActiveProcess(true);
        return true;
    }
}
