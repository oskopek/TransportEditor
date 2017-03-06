package com.oskopek.transporteditor.model.state;

public class PlanActionPointer {

    private final int actionIndex;

    private final boolean preconditionsApplied;

    public PlanActionPointer(int actionIndex, boolean preconditionsApplied) {
        this.actionIndex = actionIndex;
        this.preconditionsApplied = preconditionsApplied;
    }

    /**
     * Get the actionIndex.
     *
     * @return the actionIndex
     */
    public int getActionIndex() {
        return actionIndex;
    }

    /**
     * Get the preconditionsApplied.
     *
     * @return the preconditionsApplied
     */
    public boolean isPreconditionsApplied() {
        return preconditionsApplied;
    }
}
