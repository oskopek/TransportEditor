/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getAction())
                .append(getStartTimestamp())
                .append(getEndTimestamp())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemporalPlanAction)) {
            return false;
        }
        TemporalPlanAction that = (TemporalPlanAction) o;
        return new EqualsBuilder()
                .append(getAction(), that.getAction())
                .append(getStartTimestamp(), that.getStartTimestamp())
                .append(getEndTimestamp(), that.getEndTimestamp())
                .isEquals();
    }

    @Override
    public String toString() {
        return "TemporalPlanAction{" + "action=" + action + ", startTimestamp=" + startTimestamp + ", endTimestamp="
                + endTimestamp + '}';
    }
}
