/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

public class In extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Package aPackage = state.getPackage(action.getWhat().getName());
        Vehicle vehicle = state.getVehicle(action.getWho().getName());
        return vehicle.getPackageList().contains(aPackage);
    }
}
