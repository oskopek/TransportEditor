/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.problem.ActionObject;

import java.util.List;

public interface Action {

    String getName();

    ActionObject getWho();

    ActionObject getWhere();

    ActionObject getWhat();

    List<Predicate> getPreconditions();

    List<Predicate> getEffects();

    ActionCost getCost();

    ActionCost getDuration();

}
