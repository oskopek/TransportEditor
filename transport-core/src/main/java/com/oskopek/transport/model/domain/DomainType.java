package com.oskopek.transport.model.domain;

/**
 * The domain's type.
 */
public enum DomainType {

    /**
     * Actions are not parallel, only their order is important.
     */
    Sequential,

    /**
     * Actions have start/end times and can occur in parallel.
     */
    Temporal

}
