package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

public class HasPetrolStation extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        String locationName = action.getWhere().getName();
        return state.getRoadGraph().getNode(locationName).hasAttribute("has-petrol-station");
    }
}
