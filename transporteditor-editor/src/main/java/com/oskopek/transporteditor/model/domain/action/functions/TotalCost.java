package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TotalCost extends DefaultFunction {

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
