package com.oskopek.transport.model.domain.action;

/**
 * A predicate modifier to validate the state at specified action application time positions.
 */
public enum TemporalQuantifier {

    /**
     * Validate over the whole duration of applying given action to state (depends on other actions?).
     */
    OVER_ALL, // TODO: Depends on other actions?

    /**
     * Validate before applying given action to state.
     */
    AT_START,

    /**
     * Validate after applying given action to state.
     */
    AT_END // TODO: Remove? Can be simulated by next actions AT_START?

}
