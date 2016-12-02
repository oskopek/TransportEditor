package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import javafx.beans.property.*;
import javafx.collections.ObservableSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VariableDomainBuilder {

    private final StringProperty name = new SimpleStringProperty();

    private final SetProperty<PddlLabel> pddlLabelSet = new SimpleSetProperty<>();
    private final ObjectProperty<PddlLabel> domainType = new SimpleObjectProperty<>(PddlLabel.ActionCost);

    private final IntegerProperty pickUpCost = new SimpleIntegerProperty(1);
    private final IntegerProperty dropCost = new SimpleIntegerProperty(1);
    private final IntegerProperty refuelCost = new SimpleIntegerProperty(10);

    public VariableDomainBuilder() {
        // intentionally empty
    }

    public VariableDomain toDomain() {
        Set<PddlLabel> labels = new HashSet<>(pddlLabelSet);
        labels.add(domainType.get());

        return new VariableDomain(getName(), getDriveBuilder(), getDropBuilder(), getPickUpBuilder(),
                getRefuelBuilder(), labels, getPredicateMap(), getFunctionMap());
    }

    private RefuelBuilder getRefuelBuilder() {
        if (PddlLabel.Temporal.equals(domainType.get())) {
            return new RefuelBuilder(, ,
                    ActionCost.valueOf(refuelCost.get()), ActionCost.valueOf(refuelCost.get()));
        } else {
            return new RefuelBuilder(, ,
                    ActionCost.valueOf(refuelCost.get()), ActionCost.valueOf(refuelCost.get()));
        }
    }

    private Map<String, Class<? extends Predicate>> getPredicateMap() {
        if (PddlLabel.Temporal.equals(domainType.get())) {
            return new RefuelBuilder(, ,
                    ActionCost.valueOf(refuelCost.get()), ActionCost.valueOf(refuelCost.get()));
        } else {
            return SequentialDomain.predicateMap;
        }
    }

    private Map<String, Class<? extends Function>> getFunctionMap() {
        return null;
    }

    private PickUpBuilder getPickUpBuilder() {
        if (PddlLabel.Temporal.equals(domainType.get())) {
            return new PickUpBuilder(, ,
                    ActionCost.valueOf(pickUpCost.get()), ActionCost.valueOf(pickUpCost.get()));
        } else {
            return new PickUpBuilder(SequentialDomain.pickUpPreconditions, SequentialDomain.pickUpEffects,
                    ActionCost.valueOf(pickUpCost.get()), ActionCost.valueOf(pickUpCost.get()));
        }
    }

    private DropBuilder getDropBuilder() {
        if (PddlLabel.Temporal.equals(domainType.get())) {
            return new DropBuilder(, ,
                    ActionCost.valueOf(dropCost.get()), ActionCost.valueOf(dropCost.get()));
        } else {
            return new DropBuilder(SequentialDomain.dropPreconditions, SequentialDomain.dropEffects,
                    ActionCost.valueOf(dropCost.get()), ActionCost.valueOf(dropCost.get()));
        }
    }

    public DriveBuilder getDriveBuilder() {
        if (PddlLabel.Temporal.equals(domainType.get())) {
            return null;
        } else {
            return new SequentialDomain("").getDriveBuilder();
        }
    }

    public PddlLabel getDomainType() {
        return domainType.get();
    }

    public ObjectProperty<PddlLabel> domainTypeProperty() {
        return domainType;
    }

    public void setDomainType(PddlLabel domainType) {
        this.domainType.set(domainType);
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

    public ObservableSet<PddlLabel> getPddlLabelSet() {
        return pddlLabelSet.get();
    }

    public SetProperty<PddlLabel> pddlLabelSetProperty() {
        return pddlLabelSet;
    }

    public void setPddlLabelSet(ObservableSet<PddlLabel> pddlLabelSet) {
        this.pddlLabelSet.set(pddlLabelSet);
    }

    public int getPickUpCost() {
        return pickUpCost.get();
    }

    public IntegerProperty pickUpCostProperty() {
        return pickUpCost;
    }

    public void setPickUpCost(int pickUpCost) {
        this.pickUpCost.set(pickUpCost);
    }

    public int getDropCost() {
        return dropCost.get();
    }

    public IntegerProperty dropCostProperty() {
        return dropCost;
    }

    public void setDropCost(int dropCost) {
        this.dropCost.set(dropCost);
    }

    public int getRefuelCost() {
        return refuelCost.get();
    }

    public IntegerProperty refuelCostProperty() {
        return refuelCost;
    }

    public void setRefuelCost(int refuelCost) {
        this.refuelCost.set(refuelCost);
    }

}
