package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;

/**
 * Attributes of an edge in the {@link RoadGraph}.
 */
public interface Road extends ActionObject, Locatable {

    /**
     * Get the length.
     *
     * @return the length
     */
    ActionCost getLength();

    /**
     * Update the length, creating a new road.
     *
     * @param length the length to set
     * @return the updated road
     */
    Road updateLength(ActionCost length);

}
