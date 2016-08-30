/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SequentialPlan implements Plan {

    private final List<PlanEntry> planEntryList;

    public SequentialPlan(List<PlanEntry> planEntryList) {
        this.planEntryList = planEntryList;
    }

    public static SequentialPlan build(PlanEntry first) {
        if (first == null) {
            throw new IllegalArgumentException("First plan entry cannot be null");
        }
        List<PlanEntry> entryList = new ArrayList<>();
        PlanEntry cur = first;
        List<PlanEntry> nextEntries = cur.nextPlanEntries();
        while (nextEntries != null && nextEntries.size() > 1) {
            if (nextEntries.size() != 1) {
                throw new IllegalArgumentException("Plan is not sequential (does not have exactly 1 next plan entry)!");
            }

            List<PlanEntry> previous = cur.previousPlanEntries();
            if (previous != null && previous.size() != 1) {
                throw new IllegalArgumentException(
                        "Plan is not sequential (does not have exactly 1 previous plan entry)!");
            }

            entryList.add(cur);

            cur = nextEntries.get(0);
            nextEntries = cur.nextPlanEntries();
        }
        return new SequentialPlan(entryList);
    }

    @Override
    public List<PlanEntry> firstEntries() {
        return Collections.singletonList(planEntryList.get(0));
    }

    @Override
    public List<? extends PlanEntry> aggregatePlanEntries() {
        return planEntryList;
    }
}
