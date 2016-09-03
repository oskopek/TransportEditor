/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class DefaultDomain implements Domain {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof VariableDomain)) {
            return false;
        }

        VariableDomain that = (VariableDomain) o;

        return new EqualsBuilder().append(getPredicateList(), that.getPredicateList()).append(getFunctionList(),
                that.getFunctionList()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPredicateList()).append(getFunctionList()).toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "predicateList=" + getPredicateList() + ", functionList="
                + getFunctionList() + '}';
    }
}
