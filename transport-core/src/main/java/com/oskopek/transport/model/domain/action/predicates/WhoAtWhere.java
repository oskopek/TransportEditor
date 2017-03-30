package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;

/**
 * Asserts whether the vehicle (who) is at a given location (where).
 */
public class WhoAtWhere extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Vehicle who = state.getVehicle(action.getWho().getName());
        Location where = action.getWhere();
        return who.getLocation().equals(where);
    }
}
