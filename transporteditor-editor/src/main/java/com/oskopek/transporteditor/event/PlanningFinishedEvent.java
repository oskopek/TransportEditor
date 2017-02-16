package com.oskopek.transporteditor.event;

public class PlanningFinishedEvent {

    private final Integer selectRow;

    public PlanningFinishedEvent() {
        // intentionally empty
        this(null);
    }

    public PlanningFinishedEvent(Integer selectRow) {
        this.selectRow = selectRow;
    }

    public Integer getSelectRow() {
        return selectRow;
    }

}
