/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Problem;

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

    Problem apply(Domain domain, Problem problemState);

    default boolean arePreconditionsValid(Problem state) {
        return getPreconditions().stream().map(p -> p.isValid(state, this)).reduce(true, Boolean::logicalAnd);
    }

    default boolean areEffectsValid(Problem state) {
        return getEffects().stream().map(p -> p.isValid(state, this)).reduce(true, Boolean::logicalAnd);
    }

}
