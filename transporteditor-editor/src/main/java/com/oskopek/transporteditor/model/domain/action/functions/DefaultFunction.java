package com.oskopek.transporteditor.model.domain.action.functions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class DefaultFunction implements Function {

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getClass()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultFunction)) {
            return false;
        }
        DefaultFunction that = (DefaultFunction) obj;
        return new EqualsBuilder().append(getClass(), that.getClass()).isEquals();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
