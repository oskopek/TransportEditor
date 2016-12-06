package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.domain.VariableDomain;
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

}
