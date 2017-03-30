package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

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
