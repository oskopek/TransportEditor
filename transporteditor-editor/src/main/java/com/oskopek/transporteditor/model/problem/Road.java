package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;

/**
 * Attributes of an edge in the {@link RoadGraph}.
 */
public interface Road extends ActionObject {

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
