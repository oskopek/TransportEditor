/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class DefaultProblem implements Problem {

    private final ObjectProperty<RoadGraph> roadGraph = new SimpleObjectProperty<>();
    private final ListProperty<Vehicle> vehicleList = new SimpleListProperty<>();
    private final ListProperty<Package> packageList = new SimpleListProperty<>();

    public DefaultProblem(RoadGraph roadGraph, List<Vehicle> vehicleList, List<Package> packageList) {
        this.roadGraph.setValue(roadGraph);
        if (vehicleList == null) {
            this.vehicleList.setValue(null);
        } else {
            this.vehicleList.setValue(FXCollections.observableArrayList(vehicleList));
        }
        if (packageList == null) {
            this.packageList.setValue(null);
        } else {
            this.packageList.setValue(FXCollections.observableArrayList(packageList));
        }
    }

    @Override
    public RoadGraph getRoadGraph() {
        return roadGraph.get();
    }

    @Override
    public void setRoadGraph(RoadGraph roadGraph) {
        this.roadGraph.set(roadGraph);
    }

    @Override
    public ObjectProperty<RoadGraph> roadGraphProperty() {
        return roadGraph;
    }

    @Override
    public ObservableList<Vehicle> getVehicleList() {
        return vehicleList.get();
    }

    @Override
    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList.set(FXCollections.observableArrayList(vehicleList));
    }

    @Override
    public ListProperty<Vehicle> vehicleListProperty() {
        return vehicleList;
    }

    @Override
    public ObservableList<Package> getPackageList() {
        return packageList.get();
    }

    @Override
    public void setPackageList(List<Package> packageList) {
        this.packageList.set(FXCollections.observableArrayList(packageList));
    }

    @Override
    public ListProperty<Package> packageListProperty() {
        return packageList;
    }
}
