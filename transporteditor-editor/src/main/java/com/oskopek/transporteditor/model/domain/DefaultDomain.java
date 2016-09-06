/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

public abstract class DefaultDomain implements Domain {

    private final String name;

    private final DriveBuilder driveBuilder;
    private final DropBuilder dropBuilder;
    private final PickUpBuilder pickUpBuilder;
    private final RefuelBuilder refuelBuilder;

    private final Set<DomainLabel> domainLabelSet;

    public DefaultDomain(String name, DriveBuilder driveBuilder, DropBuilder dropBuilder, PickUpBuilder pickUpBuilder,
            RefuelBuilder refuelBuilder, Set<DomainLabel> domainLabelSet) {
        this.name = name;
        this.driveBuilder = driveBuilder;
        this.dropBuilder = dropBuilder;
        this.pickUpBuilder = pickUpBuilder;
        this.refuelBuilder = refuelBuilder;
        this.domainLabelSet = domainLabelSet;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public DriveBuilder buildDrive() {
        return driveBuilder;
    }

    @Override
    public DropBuilder buildDrop() {
        return dropBuilder;
    }

    @Override
    public PickUpBuilder buildPickUp() {
        return pickUpBuilder;
    }

    @Override
    public RefuelBuilder buildRefuel() {
        return refuelBuilder;
    }

    @Override
    public Set<DomainLabel> getDomainLabels() {
        return domainLabelSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DefaultDomain)) {
            return false;
        }

        DefaultDomain that = (DefaultDomain) o;

        return new EqualsBuilder().append(getName(), that.getName()).append(getDomainLabels(), that.getDomainLabels())
                .append(getFunctionMap(), that.getFunctionMap()).append(getPredicateMap(), that.getPredicateMap())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).append(getDomainLabels()).append(getFunctionMap()).append(
                getPredicateMap()).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("domainLabelSet", domainLabelSet).append(
                "domainLabels", getDomainLabels()).append("functionMap", getFunctionMap()).append("predicateMap",
                getPredicateMap()).toString();
    }
}
