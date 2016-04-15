/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.model;

import com.oskopek.bp.editor.model.graph.Node;

import java.util.List;

public interface TransportDomain {

    List<Vehicle> getVehicleList();
    List<Package> getPackageList();
    List<Location> getLocationList();
    Road getRoad(Location from, Location to);
    LongCost getEdgeCost(Location from, Location to);
    boolean isLocatableAt(Locatable object, Node node);

}
