package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.view.plan.GraphActionObjectDetailPopup;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.LinkedHashMap;

@Singleton
public final class GraphActionObjectDetailPopupCreator {

    @Inject
    private ResourceBundle messages;

    @Inject
    private transient Logger logger;

    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;

    private GraphActionObjectDetailPopupCreator() {
        // intentionally empty
    }

    public GraphActionObjectDetailPopup tryCreateFromLocatable(String name) {
        Problem problem = application.getPlanningSession().getProblem();
        Locatable locatable = problem.getLocatable(name);
        if (locatable instanceof Package) {
            return create((Package) locatable);
        } else if (locatable instanceof Vehicle) {
            return create((Vehicle) locatable);
        } else {
            return null;
        }
    }

    public GraphActionObjectDetailPopup create(Location location) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Name", location.getName());
        info.put("X", location.getxCoordinate() + "");
        info.put("Y", location.getyCoordinate() + "");
        return new GraphActionObjectDetailPopup(info);
    }

    public GraphActionObjectDetailPopup create(RoadGraph.RoadEdge roadEdge) {
        Map<String, String> info = new LinkedHashMap<>();
        Road road = roadEdge.getRoad();
        info.put("Name", road.getName());
        info.put("From", roadEdge.getFrom().getName());
        info.put("To", roadEdge.getTo().getName());
        info.put("Length", road.getLength().getCost() + "");
        if (road instanceof FuelRoad) {
            FuelRoad fuelRoad = (FuelRoad) road;
            info.put("Fuel cost", fuelRoad.getFuelCost().getCost() + "");
        }
        return new GraphActionObjectDetailPopup(info);
    }

    public GraphActionObjectDetailPopup create(Vehicle vehicle) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Name", vehicle.getName());
        info.put("Cur. capacity", vehicle.getCurCapacity().getCost() + "");
        info.put("Max. capacity", vehicle.getMaxCapacity().getCost() + "");
        ActionCost curFuelCapacity = vehicle.getCurFuelCapacity();
        if (curFuelCapacity != null) {
            info.put("Fuel cur. capacity", curFuelCapacity.getCost() + "");
        }
        ActionCost maxFuelCapacity = vehicle.getCurFuelCapacity();
        if (maxFuelCapacity != null) {
            info.put("Fuel max. capacity", vehicle.getMaxFuelCapacity().getCost() + "");
        }
        info.put("Package list", vehicle.getPackageList().toString());
        return new GraphActionObjectDetailPopup(info);
    }

    public GraphActionObjectDetailPopup create(Package pkg) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Name", pkg.getName());
        info.put("Size", pkg.getSize().getCost() + "");
        info.put("Target location", pkg.getTarget().getName());
        return new GraphActionObjectDetailPopup(info);
    }

}
