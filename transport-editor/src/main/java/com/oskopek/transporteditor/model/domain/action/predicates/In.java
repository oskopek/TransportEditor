package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

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
