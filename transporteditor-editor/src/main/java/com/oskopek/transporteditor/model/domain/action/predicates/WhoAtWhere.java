package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Problem;

/**
 * Asserts whether the locatable (who) is at a given location (where).
 */
public class WhoAtWhere extends DefaultPredicate {

    @Override
    public boolean isValidInternal(Problem state, Action action) {
        Locatable who = state.getLocatable(action.getWho().getName());
        Location where = action.getWhere();
        return who.getLocation().equals(where);
    }
}
