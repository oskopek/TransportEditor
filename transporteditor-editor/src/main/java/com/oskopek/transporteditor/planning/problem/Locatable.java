/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import javafx.beans.property.ObjectProperty;

public interface Locatable extends ActionObject {

    Location getLocation();

    void setLocation(Location location);

    ObjectProperty<Location> locationProperty();

}
