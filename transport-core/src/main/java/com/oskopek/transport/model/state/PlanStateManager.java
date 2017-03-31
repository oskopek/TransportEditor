package com.oskopek.transport.model.state;

import com.oskopek.transport.model.domain.action.TemporalPlanAction;

import java.util.Optional;

/**
 * Manages stepping through a plan in a problem, using a {@link PlanState} for state tracking.
 * Provides various methods for moving forward, back and jumping. Complexity of operations is up to the implementation.
 */
public interface PlanStateManager {

    /**
     * Get the current problem state.
     *
     * @return the current state
     */
    PlanState getCurrentPlanState();

    /**
     * Get the current time. Equals the sum of action durations leading up to this state in a sequential domain,
     * or the max end time of an action in a temporal domain.
     *
     * @return the current time
     */
    Double getCurrentTime();

    /**
     * Go to the specified time in the plan using the given problem instance.
     *
     * @param time the time to go to
     * @param applyStarts should we apply action "at start" effects for actions at time {@code time}?
     */
    void goToTime(Double time, boolean applyStarts);

    /**
     * Go to the next checkpoint, a time unit. Checkpoints are defined by the implementation.
     */
    void goToNextCheckpoint();

    /**
     * Go to the previous checkpoint, a time unit. Checkpoints are defined by the implementation.
     */
    void goToPreviousCheckpoint();

    /**
     * Get the last action that was executed. Can be informative only, used for visualization.
     *
     * @return the last executed action
     */
    Optional<TemporalPlanAction> getLastAction();

}
