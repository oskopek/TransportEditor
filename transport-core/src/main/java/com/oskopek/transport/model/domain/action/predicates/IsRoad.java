package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.Problem;

/**
 * The road predicate. Determines if the graph contains a given road from where to what.
 */
public class IsRoad extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        String location1 = action.getWhere().getName();
        String location2 = action.getWhat().getName();
        return state.getRoadGraph().getNode(location1).hasEdgeToward(location2);
    }
}
