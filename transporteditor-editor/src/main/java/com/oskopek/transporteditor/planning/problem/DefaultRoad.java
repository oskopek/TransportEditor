/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class DefaultRoad extends DefaultActionObject implements Road {

    private final ObjectProperty<ActionCost> length = new SimpleObjectProperty<>();

    public DefaultRoad(String name, ActionCost length) {
        super(name);
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
