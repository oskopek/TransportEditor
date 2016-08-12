package com.oskopek.transporteditor.planning.plan;

import com.oskopek.transporteditor.planning.domain.action.Action;
import com.oskopek.transporteditor.planning.domain.action.ActionCost;

import java.util.List;

public interface PlanEntry {

    Action getAction();

    ActionCost getStartTimestamp();

    default ActionCost calculateEndTimestamp() {
        return getAction().getCost().add(getStartTimestamp());
    }

    List<PlanEntry> previousPlanEntries();

    List<PlanEntry> nextPlanEntries();

}
