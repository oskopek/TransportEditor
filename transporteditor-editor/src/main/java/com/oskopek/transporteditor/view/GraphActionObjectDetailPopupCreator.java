package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.view.plan.GraphActionObjectDetailPopup;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

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

//    public GraphActionObjectDetailPopup createLocationPopup(Location location) {
//        SortedMap<String, String> info = new TreeMap<>();
//        info.put("Name", location.getName());
//        info.put("X", location.getxCoordinate() + "");
//        info.put("Y", location.getyCoordinate() + "");
//        return new GraphActionObjectDetailPopup(info);
//    }
//
//    public GraphActionObjectDetailPopup createRoadPopup(RoadGraph.RoadEdge roadEdge) {
//        SortedMap<String, String> info = new TreeMap<>();
//        info.put("Name", roadEdge.getRoad().getName());
//        info.put("From", roadEdge.getFrom().getName());
//        info.put("To", roadEdge.getTo().getName());
//        info.put("Length", roadEdge.getRoad().getLength().getCost() + "");
//        return new GraphActionObjectDetailPopup(info);
//    }
//
//    public GraphActionObjectDetailPopup createVehiclePopup(Vehicle vehicle) {
//        SortedMap<String, String> info = new TreeMap<>();
//        info.put("Name", vehicle.getName());
//        info.put("Cur. capacity", vehicle.getCurCapacity().getCost() + "");
//        info.put("Max. capacity", vehicle.getMaxCapacity().getCost() + "");
//        info.put("Fuel cur. capacity", vehicle.getCurFuelCapacity().getCost() + "");
//        info.put("Fuel max. capacity", vehicle.getMaxFuelCapacity().getCost() + "");
//        info.put("Package list", vehicle.getPackageList().toString());
//        return new GraphActionObjectDetailPopup(info);
//    }
//
//    public GraphActionObjectDetailPopup createPackagePopup(Package pkg) {
//        SortedMap<String, String> info = new TreeMap<>();
//        info.put("Name", pkg.getName());
//        info.put("Size", pkg.getSize().getCost() + "");
//        info.put("Target location", pkg.getTarget().getName());
//        return new GraphActionObjectDetailPopup(info);
//    }


    public GraphActionObjectDetailPopup create(Object object) {
        SortedMap<String, String> info = new TreeMap<>();
        for (Method method : object.getClass().getMethods()) {
            if (method.isAnnotationPresent(InfoExportable.class)) {
                try {
                    info.put(messages.getString(method.getAnnotation(InfoExportable.class).localizationKey()), method.invoke(object).toString());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.debug("Skipping method {}", method);
                }
            }
        }
        return new GraphActionObjectDetailPopup(info);
    }

}
