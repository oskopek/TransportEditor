package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.builder.*;
import com.oskopek.transporteditor.view.plan.GraphActionObjectDetailPopup;
import org.controlsfx.control.PropertySheet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.LinkedHashMap;

@Singleton
public final class GraphActionObjectDetailPopupCreator {

    @Inject
    private ResourceBundle messages;

    private GraphActionObjectDetailPopupCreator() {
        // intentionally empty
    }

    public GraphActionObjectDetailPopup tryCreateFromLocatable(Problem problem, String name) {
        Locatable locatable = problem.getLocatable(name);
        if (locatable instanceof Package) {
            return create((Package) locatable);
        } else if (locatable instanceof Vehicle) {
            return create((Vehicle) locatable);
        } else {
            return null;
        }
    }

    private GraphActionObjectDetailPopup createInternal(ActionObjectBuilder<?> builder) {
        Map<String, String> info = convertToInfoMap(LocalizableSortableBeanPropertyUtils.getProperties(builder,
                messages));
        return new GraphActionObjectDetailPopup(info);
    }

    public GraphActionObjectDetailPopup create(Location location) {
        LocationBuilder builder = new LocationBuilder();
        builder.from(location);
        return createInternal(builder);
    }

    public GraphActionObjectDetailPopup create(RoadGraph.RoadEdge roadEdge) {
        Road road = roadEdge.getRoad();
        if (road instanceof FuelRoad) {
            FuelRoad fuelRoad = (FuelRoad) road;
            FuelRoadBuilder builder = new FuelRoadBuilder();
            builder.from(fuelRoad);
            return createInternal(builder);
        } else {
            DefaultRoadBuilder<DefaultRoad> builder = new DefaultRoadBuilder<>();
            builder.from((DefaultRoad) road);
            return createInternal(builder);
        }
    }

    public GraphActionObjectDetailPopup create(Vehicle vehicle) {
        VehicleBuilder builder = new VehicleBuilder();
        builder.from(vehicle);
        return createInternal(builder);
    }

    public GraphActionObjectDetailPopup create(Package pkg) {
        PackageBuilder builder = new PackageBuilder();
        builder.from(pkg);
        return createInternal(builder);
    }

    private LinkedHashMap<String, String> convertToInfoMap(List<PropertySheet.Item> items) {
        LinkedHashMap<String, String> info = new LinkedHashMap<>(items.size());
        items.forEach(item -> info.put(item.getName(), item.getValue().toString()));
        return info;
    }

}
