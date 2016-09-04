/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan;

import com.oskopek.transporteditor.planning.domain.action.Action;
import com.oskopek.transporteditor.planning.domain.action.ActionCost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultPlanEntry implements PlanEntry {

    private final List<PlanEntry> previousPlanEntries = new ArrayList<>();
    private final List<PlanEntry> nextPlanEntries = new ArrayList<>();
    private final Action action;
    private final ActionCost startTimetamp;

    public DefaultPlanEntry(Action action, ActionCost startTimetamp) {
        this.action = action;
        this.startTimetamp = startTimetamp;
    }

    public void addNextPlanEntry(PlanEntry planEntry) {
        nextPlanEntries.add(planEntry);
    }

    public void addAllNextPlanEntry(Collection<PlanEntry> planEntry) {
        nextPlanEntries.addAll(planEntry);
    }

    public void addPreviousPlanEntry(PlanEntry planEntry) {
        previousPlanEntries.add(planEntry);
    }

    public void addAllPreviousPlanEntry(Collection<PlanEntry> planEntry) {
        previousPlanEntries.addAll(planEntry);
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public ActionCost getStartTimestamp() {
        return startTimetamp;
    }

    @Override
    public List<PlanEntry> previousPlanEntries() {
        return previousPlanEntries;
    }

    @Override
    public List<PlanEntry> nextPlanEntries() {
        return nextPlanEntries;
    }
}
