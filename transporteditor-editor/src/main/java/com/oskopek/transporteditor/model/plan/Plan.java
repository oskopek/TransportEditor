/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;

import java.util.Collection;

public interface Plan {

    Collection<Action> getAllActions();

    Collection<TemporalPlanAction> getTemporalPlanActions();

}
