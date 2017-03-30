package com.oskopek.transporteditor.event;

/**
 * Event for announcing that a planning process in a {@link com.oskopek.transporteditor.model.planner.Planner}
 * was completed.
 */
public final class PlanningFinishedEvent {

    private final Integer selectRow;

    /**
     * Default constructor with no selection.
     */
    public PlanningFinishedEvent() {
        // intentionally empty
        this(null);
    }

    /**
     * Constructor with a row to select.
     *
     * @param selectRow the row that should be selected after updating the view
     */
    public PlanningFinishedEvent(Integer selectRow) {
        this.selectRow = selectRow;
    }

    /**
     * Get the row to select.
     *
     * @return the row to select, nullable (no row to select)
     */
    public Integer getSelectRow() {
        return selectRow;
    }

}
