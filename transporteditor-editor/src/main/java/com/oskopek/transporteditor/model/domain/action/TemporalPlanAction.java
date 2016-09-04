/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;


public class TemporalPlanAction {

    private final Action action;

    private final Integer startTimestamp;
    private final Integer endTimestamp;

    public TemporalPlanAction(Action action, Integer startTimestamp, Integer endTimestamp) {
        this.action = action;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public Integer getStartTimestamp() {
        return startTimestamp;
    }

    public Integer getEndTimestamp() {
        return endTimestamp;
    }

    public Action getAction() {
        return action;
    }
}
