/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.plan.visualization.PlanState;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.Locatable;
import com.oskopek.transporteditor.planning.problem.Location;

import java.util.List;

public interface Action {

    String getName();

    Locatable getWho();

    Location getWhere();

    ActionObject getWhat();

    List<Predicate> getPreconditions();

    List<Predicate> getEffects();

    ActionCost getCost();

    ActionCost getDuration();

    default boolean arePreconditionsValid(PlanState state) {
        return getPreconditions().stream().map(p -> p.isValid(state)).reduce(true, Boolean::logicalAnd);
    }

    default boolean areEffectsValid(PlanState state) {
        return getEffects().stream().map(p -> p.isValid(state)).reduce(true, Boolean::logicalAnd);
    }

}
