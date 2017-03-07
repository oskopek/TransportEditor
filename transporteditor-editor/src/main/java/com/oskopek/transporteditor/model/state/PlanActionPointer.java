package com.oskopek.transporteditor.model.state;

public class PlanActionPointer {

    private final int time;

    private final boolean startsApplied;

    public PlanActionPointer(int time, boolean startsApplied) {
        this.time = time;
        this.startsApplied = startsApplied;
    }

    /**
     * Get the time.
     *
     * @return the time
     */
    public int getTime() {
        return time;
    }

    /**
     * Get the startsApplied.
     *
     * @return the startsApplied
     */
    public boolean isStartsApplied() {
        return startsApplied;
    }
}
