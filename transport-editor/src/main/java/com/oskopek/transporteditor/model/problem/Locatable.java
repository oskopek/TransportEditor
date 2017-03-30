package com.oskopek.transporteditor.model.problem;

/**
 * An action object with a an associated location.
 */
public interface Locatable extends ActionObject {

    /**
     * Get the location.
     *
     * @return the location
     */
    Location getLocation();

}
