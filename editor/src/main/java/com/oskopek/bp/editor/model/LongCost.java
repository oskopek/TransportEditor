/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.model;

import com.oskopek.bp.editor.model.graph.Cost;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class LongCost implements Cost {

    private long cost;

    public long getValue() {
        return cost;
    }

    public LongCost(long cost) {
        this.cost = cost;
    }



    @Override
    public String toString() {
        return "LongCost{" +
                "cost=" + cost +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof LongCost)) return false;

        LongCost longCost = (LongCost) o;

        return new EqualsBuilder()
                .append(getValue(), longCost.getValue())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getValue())
                .toHashCode();
    }
}
