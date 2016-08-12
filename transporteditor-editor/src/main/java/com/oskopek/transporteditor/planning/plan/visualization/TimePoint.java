/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan.visualization;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.domain.action.predicates.TemporalQuantifier;
import com.oskopek.transporteditor.planning.plan.PlanEntry;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * All properties needed, even in a sequential domain (it makes sense if you think about it).
 */
public final class TimePoint {

    private final ActionCost timestamp;
    private final TemporalQuantifier temporalQuantifier;
    private final PlanEntry planEntry;

    public TimePoint(ActionCost timestamp, TemporalQuantifier temporalQuantifier, PlanEntry planEntry) {
        this.timestamp = timestamp;
        this.temporalQuantifier = temporalQuantifier;
        this.planEntry = planEntry;
    }

    public ActionCost getTimestamp() {
        return timestamp;
    }

    public TemporalQuantifier getTemporalQuantifier() {
        return temporalQuantifier;
    }

    public PlanEntry getPlanEntry() {
        return planEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimePoint)) {
            return false;
        }
        TimePoint timePoint = (TimePoint) o;
        return new EqualsBuilder().append(getTimestamp(), timePoint.getTimestamp()).append(getTemporalQuantifier(),
                timePoint.getTemporalQuantifier()).append(getPlanEntry(), timePoint.getPlanEntry()).isEquals();
    }

    @Override
    public String toString() {
        return "TimePoint{" + "timestamp=" + timestamp + ", temporalQuantifier=" + temporalQuantifier + ", planEntry="
                + planEntry + '}';
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getTimestamp()).append(getTemporalQuantifier()).append(getPlanEntry())
                .toHashCode();
    }
}
