package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import javafx.beans.property.*;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

public final class VariableDomainBuilder {

    private final StringProperty name = new SimpleStringProperty();

    private final ObjectProperty<DriveBuilder> driveBuilder = new SimpleObjectProperty<>();
    private final ObjectProperty<DropBuilder> dropBuilder = new SimpleObjectProperty<>();
    private final ObjectProperty<PickUpBuilder> pickUpBuilder = new SimpleObjectProperty<>();
    private final ObjectProperty<RefuelBuilder> refuelBuilder = new SimpleObjectProperty<>();

    private final SetProperty<PddlLabel> pddlLabelSet = new SimpleSetProperty<>();

    private final MapProperty<String, Class<? extends Predicate>> predicateMap = new SimpleMapProperty<>();
    private final MapProperty<String, Class<? extends Function>> functionMap = new SimpleMapProperty<>();

    public VariableDomainBuilder() {
        // intentionally empty
    }

    public VariableDomain toDomain() {
        return new VariableDomain(getName(), getDriveBuilder(), getDropBuilder(), getPickUpBuilder(),
                getRefuelBuilder(), getPddlLabelSet(), getPredicateMap(), getFunctionMap());
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public DriveBuilder getDriveBuilder() {
        return driveBuilder.get();
    }

    public void setDriveBuilder(DriveBuilder driveBuilder) {
        this.driveBuilder.set(driveBuilder);
    }

    public ObjectProperty<DriveBuilder> driveBuilderProperty() {
        return driveBuilder;
    }

    public DropBuilder getDropBuilder() {
        return dropBuilder.get();
    }

    public void setDropBuilder(DropBuilder dropBuilder) {
        this.dropBuilder.set(dropBuilder);
    }

    public ObjectProperty<DropBuilder> dropBuilderProperty() {
        return dropBuilder;
    }

    public PickUpBuilder getPickUpBuilder() {
        return pickUpBuilder.get();
    }

    public void setPickUpBuilder(PickUpBuilder pickUpBuilder) {
        this.pickUpBuilder.set(pickUpBuilder);
    }

    public ObjectProperty<PickUpBuilder> pickUpBuilderProperty() {
        return pickUpBuilder;
    }

    public RefuelBuilder getRefuelBuilder() {
        return refuelBuilder.get();
    }

    public void setRefuelBuilder(RefuelBuilder refuelBuilder) {
        this.refuelBuilder.set(refuelBuilder);
    }

    public ObjectProperty<RefuelBuilder> refuelBuilderProperty() {
        return refuelBuilder;
    }

    public ObservableSet<PddlLabel> getPddlLabelSet() {
        return pddlLabelSet.get();
    }

    public void setPddlLabelSet(ObservableSet<PddlLabel> pddlLabelSet) {
        this.pddlLabelSet.set(pddlLabelSet);
    }

    public SetProperty<PddlLabel> pddlLabelSetProperty() {
        return pddlLabelSet;
    }

    public ObservableMap<String, Class<? extends Predicate>> getPredicateMap() {
        return predicateMap.get();
    }

    public void setPredicateMap(ObservableMap<String, Class<? extends Predicate>> predicateMap) {
        this.predicateMap.set(predicateMap);
    }

    public MapProperty<String, Class<? extends Predicate>> predicateMapProperty() {
        return predicateMap;
    }

    public ObservableMap<String, Class<? extends Function>> getFunctionMap() {
        return functionMap.get();
    }

    public void setFunctionMap(ObservableMap<String, Class<? extends Function>> functionMap) {
        this.functionMap.set(functionMap);
    }

    public MapProperty<String, Class<? extends Function>> functionMapProperty() {
        return functionMap;
    }

}
