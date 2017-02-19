package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;

/**
 * Simple interface for mutating a problem's state by applying actions on top of it.
 */
public interface PlanState extends Problem {

    /**
     * Changes the internal state of the problem by applying the specified action.
     *
     * @param action the action to apply
     */
    void apply(Action action);

}
