package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;

import java.util.Optional;

public interface PlanStateManager {

    PlanState getCurrentPlanState();

    ActionCost getCurrentTime();

    void goToTime(ActionCost time, boolean applyStarts);

    void goToNextCheckpoint();

    void goToPreviousCheckpoint();

    Optional<TemporalPlanAction> getLastAction();

}
