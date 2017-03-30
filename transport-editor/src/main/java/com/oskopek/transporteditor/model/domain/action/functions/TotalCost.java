package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The total-cost global function. Returns the the current total cost of the plan.
 */
public class TotalCost extends DefaultFunction {
    // TODO how do we actually return the total cost of the plan? Do we even need this function? Only as a placeholder?

    private final ObjectProperty<ActionCost> totalCost = new SimpleObjectProperty<>(ActionCost.valueOf(0));

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 0) {
            throw new IllegalArgumentException("TotalCost takes no arguments");
        }
        return apply();
    }

    /**
     * Gets the the current total cost of the plan.
     *
     * @return the total cost of the plan
     */
    public ActionCost apply() {
        return totalCost.get();
    }
}
