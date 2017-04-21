package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.*;

/**
 * The in predicate. Determines if the state has a specific vehicle (what) containing a specific package (who).
 */
public class In extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Package aPackage = state.getPackage(action.getWhat().getName());
        Vehicle vehicle = state.getVehicle(action.getWho().getName());
        return vehicle.getPackageList().contains(aPackage);
    }
}
