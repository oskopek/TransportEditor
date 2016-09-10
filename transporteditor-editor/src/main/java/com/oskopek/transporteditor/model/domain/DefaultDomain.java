/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.Drop;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.domain.action.Refuel;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
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
    public Drive buildDrive(Vehicle vehicle, Location from, Location to, Road road) {
        return driveBuilder.build(vehicle, from, to, road);
    }

    @Override
    public Drive buildDrive(Vehicle vehicle, Location from, Location to, RoadGraph graph) {
        return driveBuilder.build(vehicle, from, to, graph);
    }

    @Override
    public Drop buildDrop(Vehicle vehicle, Location at, Package what) {
        return dropBuilder.build(vehicle, at, what);
    }

    @Override
    public PickUp buildPickUp(Vehicle vehicle, Location at, Package what) {
        return pickUpBuilder.build(vehicle, at, what);
    }

    @Override
    public Refuel buildRefuel(Vehicle vehicle, Location at) {
        return refuelBuilder.build(vehicle, at);
    }

    @Override
    public DriveBuilder getDriveBuilder() {
        return driveBuilder;
    }

    @Override
    public DropBuilder getDropBuilder() {
        return dropBuilder;
    }

    @Override
    public PickUpBuilder getPickUpBuilder() {
        return pickUpBuilder;
    }

    @Override
    public RefuelBuilder getRefuelBuilder() {
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
