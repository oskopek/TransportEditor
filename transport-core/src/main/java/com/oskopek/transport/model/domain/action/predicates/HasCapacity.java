package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;

/**
 * Assert that a vehicle (who) has a current capacity (current available space) to pick up package (what).
 */
public class HasCapacity extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Vehicle vehicle = state.getVehicle(action.getWho().getName());
        Package aPackage = state.getPackage(action.getWhat().getName());
        return vehicle.getCurCapacity().subtract(aPackage.getSize()).getCost() >= 0;
    }
}
