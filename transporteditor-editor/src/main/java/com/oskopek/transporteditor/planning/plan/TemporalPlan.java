/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;

import java.util.*;
import java.util.stream.Collectors;

public class TemporalPlan implements Plan {

    private SortedMap<ActionCost, List<PlanEntry>> planEntryMap;

    public TemporalPlan(List<PlanEntry> planEntryList) {
        planEntryMap = new TreeMap<>();
        for (PlanEntry item : planEntryList) {
            List<PlanEntry> previous = planEntryMap.putIfAbsent(item.getStartTimestamp(),
                    new ArrayList<>(Collections.singletonList(item)));
            if (previous != null) {
                previous.add(item);
                planEntryMap.put(item.getStartTimestamp(), previous);
            }
        }
    }

    @Override
    public List<PlanEntry> firstEntries() {
        return planEntryMap.get(planEntryMap.firstKey());
    }

    @Override
    public List<? extends PlanEntry> aggregatePlanEntries() {
        return planEntryMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
