/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import com.oskopek.transporteditor.planning.domain.action.predicates.sequential.Capacity;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class Vehicle extends DefaultLocatable implements Locatable, ActionObject {

    private final ObjectProperty<Capacity> curCapacity = new SimpleObjectProperty<>();
    private final ObjectProperty<Capacity> maxCapacity = new SimpleObjectProperty<>();
    private final ListProperty<Package> packageList = new SimpleListProperty<>();

    public Vehicle(String name, Location location, Capacity curCapacity, Capacity maxCapacity,
            List<Package> packageList) {
        super(name, location);
        this.curCapacity.setValue(curCapacity);
        this.maxCapacity.setValue(maxCapacity);
        if (packageList == null) {
            this.packageList.setValue(null);
        } else {
            this.packageList.setValue(FXCollections.observableArrayList(packageList));
        }
    }

    public Capacity getCurCapacity() {
        return curCapacity.get();
    }

    public void setCurCapacity(Capacity curCapacity) {
        this.curCapacity.set(curCapacity);
    }

    public ObjectProperty<Capacity> curCapacityProperty() {
        return curCapacity;
    }

    public Capacity getMaxCapacity() {
        return maxCapacity.get();
    }

    public void setMaxCapacity(Capacity maxCapacity) {
        this.maxCapacity.set(maxCapacity);
    }

    public ObjectProperty<Capacity> maxCapacityProperty() {
        return maxCapacity;
    }

    public ObservableList<Package> getPackageList() {
        return packageList.get();
    }

    public void setPackageList(ObservableList<Package> packageList) {
        this.packageList.set(packageList);
    }

    public ListProperty<Package> packageListProperty() {
        return packageList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle)) {
            return false;
        }
        Vehicle vehicle = (Vehicle) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(getCurCapacity(), vehicle.getCurCapacity())
                .append(getMaxCapacity(), vehicle.getMaxCapacity()).append(getPackageList(), vehicle.getPackageList())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getCurCapacity()).append(
                getMaxCapacity()).append(getPackageList()).toHashCode();
    }

    @Override
    public String toString() {
        return "Vehicle[" + getName() + ", at=" + getLocation() + ", capacity=" + getCurCapacity() + "/"
                + getMaxCapacity() + ", packages=" + getPackageList() + "]";
    }
}