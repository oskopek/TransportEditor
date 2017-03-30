package com.oskopek.transporteditor.model.domain.action;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Wrapper method for an immutable whole number with default value 0. The actual internal type could be changed.
 */
public final class ActionCost implements Comparable<ActionCost> {

    private final Integer cost;

    /**
     * Default boxed constructor.
     *
     * @param cost the cost
     */
    private ActionCost(Integer cost) {
        if (cost == null) {
            this.cost = 0;
        } else {
            this.cost = cost;
        }
    }

    /**
     * Default constructor.
     *
     * @param cost the cost
     */
    private ActionCost(int cost) {
        this.cost = cost;
    }

    /**
     * Boxed builder method.
     *
     * @param cost the cost
     * @return an associated action cost object
     */
    public static ActionCost valueOf(Integer cost) {
        return new ActionCost(cost);
    }

    /**
     * Builder method.
     *
     * @param cost the cost
     * @return an associated action cost object
     */
    public static ActionCost valueOf(int cost) {
        return new ActionCost(cost);
    }

    /**
     * Add the cost to our own value and return the resulting cost.
     *
     * @param cost the other cost
     * @return the sum, non-null and immutable
     */
    public ActionCost add(ActionCost cost) {
        return ActionCost.valueOf(getCost() + cost.getCost());
    }

    /**
     * Negate the cost of our own value and return the resulting cost.
     *
     * @return the negation, non-null and immutable
     */
    public ActionCost neg() {
        return ActionCost.valueOf(-getCost());
    }

    /**
     * Subtract the other cost from our own value and return the resulting cost.
     *
     * @param cost the other cost
     * @return the subtracted result, non-null and immutable
     */
    public ActionCost subtract(ActionCost cost) {
        return add(cost.neg());
    }

    /**
     * Get the internal value.
     *
     * @return the internal value
     */
    public int getCost() {
        return cost;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getCost()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActionCost)) {
            return false;
        }
        ActionCost that = (ActionCost) o;
        return new EqualsBuilder().append(getCost(), that.getCost()).isEquals();
    }

    @Override
    public String toString() {
        return Integer.toString(cost);
    }

    @Override
    public int compareTo(ActionCost o) {
        return new CompareToBuilder().append(cost, o.cost).toComparison();
    }
}
