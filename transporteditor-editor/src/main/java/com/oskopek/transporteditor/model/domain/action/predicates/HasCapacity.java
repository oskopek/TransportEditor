/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

/**
 * Assert a vehicle has a current capacity (current available space) of x.
 */
public class HasCapacity extends DefaultPredicate {

    @Override
    public boolean isValid(Problem state, Action action) {
        Vehicle vehicle = state.getVehicle(action.getWho().getName());
        Package aPackage = state.getPackage(action.getWhat().getName());
        return vehicle.getCurCapacity().subtract(aPackage.getSize()).getCost() >= 0;
    }
}
