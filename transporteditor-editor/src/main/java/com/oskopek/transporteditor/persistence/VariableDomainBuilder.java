package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import javafx.beans.property.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class VariableDomainBuilder {

    private final StringProperty name = new SimpleStringProperty();

    // TODO: Create a domaintype enum?
    private final ObjectProperty<PddlLabel> domainType = new SimpleObjectProperty<>(PddlLabel.ActionCost);

    private final BooleanProperty fuel = new SimpleBooleanProperty();
    private final BooleanProperty capacity = new SimpleBooleanProperty();
    private final BooleanProperty numeric = new SimpleBooleanProperty();

    private final IntegerProperty pickUpCost = new SimpleIntegerProperty(1);
    private final IntegerProperty dropCost = new SimpleIntegerProperty(1);
    private final IntegerProperty refuelCost = new SimpleIntegerProperty(10);

    private final StringProperty goalText = new SimpleStringProperty();
    private final StringProperty metricText = new SimpleStringProperty();

    private final VariableDomainIO domainIO = new VariableDomainIO();

    public VariableDomainBuilder() {
        // intentionally empty
    }

    public VariableDomain toDomain() {
        String domainName = calculateDomainFilename();
        String domain;
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(domainName)))) {
            domain = buffer.lines().collect(Collectors.joining("\n")) + "\n";
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file \"" + domainName + "\" for creating a domain.", e);
        }
        return domainIO.parse(domain);
    }

    private String calculateDomainFilename() {
        StringBuilder builder = new StringBuilder("domain-variants").append(File.separator);
        builder.append(PddlLabel.ActionCost.equals(domainType.get()) ? "sequential" : "temporal");
        builder.append(File.separator).append("domain").append(File.separator).append("domain-");

        builder.append(isCapacity() ? "" : "No").append("Cap");
        builder.append("-");
        builder.append(isFuel() ? "" : "No").append("Fuel");
        builder.append("-");
        builder.append(isNumeric() ? "" : "No").append("Num");

        builder.append(".pddl");
        return builder.toString();
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

    public PddlLabel getDomainType() {
        return domainType.get();
    }

    public void setDomainType(PddlLabel domainType) {
        this.domainType.set(domainType);
    }

    public ObjectProperty<PddlLabel> domainTypeProperty() {
        return domainType;
    }

    public boolean isFuel() {
        return fuel.get();
    }

    public void setFuel(boolean fuel) {
        this.fuel.set(fuel);
    }

    public BooleanProperty fuelProperty() {
        return fuel;
    }

    public boolean isCapacity() {
        return capacity.get();
    }

    public void setCapacity(boolean capacity) {
        this.capacity.set(capacity);
    }

    public BooleanProperty capacityProperty() {
        return capacity;
    }

    public boolean isNumeric() {
        return numeric.get();
    }

    public void setNumeric(boolean numeric) {
        this.numeric.set(numeric);
    }

    public BooleanProperty numericProperty() {
        return numeric;
    }

    public int getPickUpCost() {
        return pickUpCost.get();
    }

    public void setPickUpCost(int pickUpCost) {
        this.pickUpCost.set(pickUpCost);
    }

    public IntegerProperty pickUpCostProperty() {
        return pickUpCost;
    }

    public int getDropCost() {
        return dropCost.get();
    }

    public void setDropCost(int dropCost) {
        this.dropCost.set(dropCost);
    }

    public IntegerProperty dropCostProperty() {
        return dropCost;
    }

    public int getRefuelCost() {
        return refuelCost.get();
    }

    public void setRefuelCost(int refuelCost) {
        this.refuelCost.set(refuelCost);
    }

    public IntegerProperty refuelCostProperty() {
        return refuelCost;
    }

    public String getGoalText() {
        return goalText.get();
    }

    public void setGoalText(String goalText) {
        this.goalText.set(goalText);
    }

    public StringProperty goalTextProperty() {
        return goalText;
    }

    public String getMetricText() {
        return metricText.get();
    }

    public void setMetricText(String metricText) {
        this.metricText.set(metricText);
    }

    public StringProperty metricTextProperty() {
        return metricText;
    }
}
