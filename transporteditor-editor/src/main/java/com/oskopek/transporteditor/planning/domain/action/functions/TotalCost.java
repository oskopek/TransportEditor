/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.functions;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TotalCost implements Function {

    private final ObjectProperty<ActionCost> totalCost = new SimpleObjectProperty<>(ActionCost.valueOf(0));

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 0) {
            throw new IllegalArgumentException("TotalCost takes no arguments");
        }
        return apply();
    }

    public ActionCost apply() {
        return totalCost.get();
    }
}
