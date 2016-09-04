/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;

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
}
