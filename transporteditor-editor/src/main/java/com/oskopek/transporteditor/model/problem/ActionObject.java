/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import javafx.beans.property.StringProperty;

public interface ActionObject {

    String getName();

    void setName(String name);

    StringProperty nameProperty();

}
