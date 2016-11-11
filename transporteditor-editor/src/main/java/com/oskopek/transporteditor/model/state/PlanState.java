package com.oskopek.transporteditor.model.state;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

public interface PlanState extends Problem {

    void apply(Action action);

    default Vehicle getVehicleSafe(String name) {
        Vehicle vehicle = getVehicle(name);
        if (vehicle == null) {
            throw new IllegalArgumentException("Could not find vehicle with name \"" + name + "\".");
        }
        return vehicle;
    }
}
