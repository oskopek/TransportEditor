package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;

import java.util.Collection;

public interface Plan {

    Collection<Action> getActions();

    Collection<TemporalPlanAction> getTemporalPlanActions();

}
