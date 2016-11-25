package com.oskopek.transporteditor.model.domain.action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class ActionCost {

    private final Integer cost;

    private ActionCost(Integer cost) {
        if (cost == null) {
            this.cost = 0;
        } else {
            this.cost = cost;
        }
    }

    public static ActionCost valueOf(Integer cost) {
        return new ActionCost(cost);
    }

    public ActionCost add(ActionCost cost) {
        return ActionCost.valueOf(getCost() + cost.getCost());
    }

    public ActionCost neg(ActionCost cost) {
        return ActionCost.valueOf(-cost.getCost());
    }

    public ActionCost subtract(ActionCost cost) {
        return add(neg(cost));
    }

    public Integer getCost() {
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
}
