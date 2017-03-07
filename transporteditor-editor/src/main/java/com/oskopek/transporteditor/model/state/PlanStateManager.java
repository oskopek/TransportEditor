package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.action.ActionCost;

public interface PlanStateManager {

    PlanState getCurrentPlanState();

    ActionCost getCurrentTime();

    void goToTime(ActionCost time, boolean applyStarts);

    void goToNextCheckpoint();

    void goToPreviousCheckpoint();

}
