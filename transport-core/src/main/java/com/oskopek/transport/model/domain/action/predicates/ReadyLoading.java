package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;

/**
 * The ready-loading predicate. Determines if a vehicle is not loading or dropping at the moment.
 */
public class ReadyLoading extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Vehicle what = state.getVehicle(action.getWhat().getName());
        return what.isReadyLoading();
    }
}
