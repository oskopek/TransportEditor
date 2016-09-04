/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;

public class DefaultRoad extends DefaultActionObject implements Road {

    private final ActionCost length;

    public DefaultRoad(String name, ActionCost length) {
        super(name);
        this.length = length;
    }

    @Override
    public ActionCost getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "Road[" + getName() + ": " + getLength() + ']';
    }
}
