/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;

public abstract class DefaultDomain implements Domain {

    private final String name;
    private final DefaultActionBuilder driveBuilder;
    private final DropBuilder dropAcBuilder
    private final PickUpBuilder pickUpBuilder;
    private final RefuelBuilder refuelBuilder;

    public DefaultDomain(String name, Class<? extends Drive> driveAction, Class<? extends Drop> dropAction,
            Class<? extends PickUp> pickUpAction) {
        this(name, driveAction, dropAction, pickUpAction, null);
    }

    public DefaultDomain(String name, Class<? extends Drive> driveAction, Class<? extends Drop> dropAction,
            Class<? extends PickUp> pickUpAction, Class<? extends Refuel> refuelAction) {
        this.name = name;
        this.driveAction = driveAction;
        this.dropAction = dropAction;
        this.pickUpAction = pickUpAction;
        this.refuelAction = refuelAction;

        actionList = new ArrayList<>();
        actionList.add((Class<Action>) driveAction);
        actionList.add((Class<Action>) dropAction);
        actionList.add((Class<Action>) pickUpAction);
        actionList.add((Class<Action>) refuelAction);

    }

    @Override
    public String getName() {
        return name;
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

        return new EqualsBuilder().append(getName(), that.getName()).append(getDriveAction(), that.getDriveAction())
                .append(getDropAction(), that.getDropAction()).append(getPickUpAction(), that.getPickUpAction()).append(
                        getRefuelAction(), that.getRefuelAction()).append(getDomainType(), that.getDomainType()).append(
                        getPredicateList(), that.getPredicateList()).append(getFunctionList(), that.getFunctionList())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).append(getDriveAction()).append(getDropAction()).append(
                getPickUpAction()).append(getRefuelAction()).append(getDomainType()).append(getPredicateList()).append(
                getFunctionList()).toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + getName() + ", domainType=" + getDomainType() + ", predicateList="
                + getPredicateList() + ", functionList=" + getFunctionList() + ", actionList=" + getActionList() + '}';
    }
}
