package com.oskopek.transport.model.domain;

/**
 * Domain labels used for determining the individual "layers" (features) it enables.
 */
public enum PddlLabel {

    /**
     * The action cost PDDL requirement. Provides costs for actions and minimizes the total sum of them.
     */
    ActionCost,

    /**
     * A numeric optimization of a metric and a goal, specified by a custom predicate/function.
     */
    Numeric,

    /**
     * Time is taken into account, not only the sequence of actions. Actions have durations.
     */
    Temporal,

    /**
     * Fuel constraints are taken into account. Vehicle have fuel capacities, roads have fuel costs.
     */
    Fuel,

    /**
     * Vehicles have a capacity.
     */
    Capacity,

    /**
     * Vehicles have a maximum capacity (are not unbounded).
     */
    MaxCapacity

}
