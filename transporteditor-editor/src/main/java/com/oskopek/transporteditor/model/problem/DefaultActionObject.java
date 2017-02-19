package com.oskopek.transporteditor.model.problem;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class DefaultActionObject implements ActionObject {

    private final String name;

    public DefaultActionObject(String name) {
        if (name == null || !name.equals(name.toLowerCase())) {
            throw new IllegalArgumentException("Name of action objects has to be lower case and non null.");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultActionObject)) {
            return false;
        }
        DefaultActionObject that = (DefaultActionObject) o;
        return new EqualsBuilder().append(getName(), that.getName()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).toString();
    }
}
