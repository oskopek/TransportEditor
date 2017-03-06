package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.action.ActionCost;

public interface PlanStateManager {

    PlanState getCurrentPlanState();

    ActionCost getCurrentTime();

    void goToTime(ActionCost time);

    void goToTimeRightAfter(ActionCost time);

    void goToNextCheckpoint();

}
