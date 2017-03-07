package com.oskopek.transporteditor.model.state;

/**
 * Simple data struct used for keeping track of the current position in a
 * {@link com.oskopek.transporteditor.model.plan.Plan} in {@link PlanState}s.
 */
public class PlanActionPointer {

    private final int time;

    private final boolean startsApplied;

    /**
     * Default constructor.
     *
     * @param time the time to pointing to
     * @param startsApplied are "at start" effects of actions at time {@code time} applied to this state?
     */
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
     * Are "at start" effects of actions at time {@code time} applied to this state?
     *
     * @return the startsApplied
     */
    public boolean isStartsApplied() {
        return startsApplied;
    }
}
