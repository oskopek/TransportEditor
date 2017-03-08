package com.oskopek.transporteditor.model.problem;

/**
 * Represents an action object of the problem (vehicles, packages, locations, roads...).
 */
public interface ActionObject {

    /**
     * Get the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Update the name, returning a new action object.
     *
     * @param newName the new name
     * @return the updated action object
     */
    ActionObject updateName(String newName);

}
