/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class DefaultRoad implements Road {

    private final ObjectProperty<ActionCost> length = new SimpleObjectProperty<>();

    public DefaultRoad(ActionCost length) {
        this.length.setValue(length);
    }

    @Override
    public ActionCost getLength() {
        return length.getValue();
    }

    @Override
    public void setLength(ActionCost length) {
        this.length.set(length);
    }

    @Override
    public ObjectProperty<ActionCost> lengthProperty() {
        return length;
    }
}
