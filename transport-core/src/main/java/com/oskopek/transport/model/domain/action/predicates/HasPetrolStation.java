package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Problem;

/**
 * Asserts if a given location (where) has a petrol station.
 */
public class HasPetrolStation extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        String locationName = action.getWhere().getName();
        return state.getRoadGraph().getLocation(locationName).getPetrolStation();
    }
}
