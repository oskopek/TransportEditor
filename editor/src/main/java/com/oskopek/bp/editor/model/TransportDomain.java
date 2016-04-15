/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.model;

import java.util.List;

public interface TransportDomain {

    List<Vehicle> getVehicleList();
    List<Package> getPackageList();
    List<Location> getLocationList();
    Edge get
    LongCost getEdgeCost(Location from, Location to);
    boolean isLocatableAt(Locatable object, Location location);

}
