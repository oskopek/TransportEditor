package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

/**
 * Simple interface for mutating a problem's state by applying actions on top of it.
 */
public interface PlanState extends Problem {

    /**
     * Changes the internal state of the problem by applying the specified action.
     *
     * @param action the action to apply
     */
    default void apply(Action action) {
        applyPreconditions(action);
        applyEffects(action);
    }

    /**
     * Changes the internal state of the problem by applying effects specified as "at start".
     *
     * @param action the action whose parts to apply
     */
    void applyPreconditions(Action action);

    /**
     * Changes the internal state of the problem by applying effects specified as "at end".
     *
     * @param action the action whose parts to apply
     */
    void applyEffects(Action action);

    /**
     * Check if this state is a goal state.
     *
     * @return true iff this state is a goal state, i.e. if all packages and vehicles are at their targets, if specified
     */
    default boolean isGoalState() {
        for (Package p : getAllPackages()) {
            if (!p.getTarget().equals(p.getLocation())) {
                return false;
            }
        }
        for (Vehicle v : getAllVehicles()) {
            Location target = v.getTarget();
            if (target != null && !target.equals(v.getLocation())) {
                return false;
            }
        }
        return true;
    }

}
