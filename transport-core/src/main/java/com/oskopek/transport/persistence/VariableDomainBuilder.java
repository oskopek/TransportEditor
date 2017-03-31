package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.DomainType;
import com.oskopek.transport.model.domain.VariableDomain;
import javafx.beans.property.*;

import java.io.IOException;

/**
 * The type Variable domain builder.
 */
public final class VariableDomainBuilder {

    private final StringProperty name = new SimpleStringProperty();

    private final ObjectProperty<DomainType> domainType = new SimpleObjectProperty<>(DomainType.Sequential);

    private final BooleanProperty fuel = new SimpleBooleanProperty();
    private final BooleanProperty capacity = new SimpleBooleanProperty();
    private final BooleanProperty numeric = new SimpleBooleanProperty();

    private final StringProperty goalText = new SimpleStringProperty();
    private final StringProperty metricText = new SimpleStringProperty();

    private final VariableDomainIO domainIO = new VariableDomainIO();

    /**
     * Default constructor.
     */
    public VariableDomainBuilder() {
        // intentionally empty
    }

    /**
     * Build the domain from the internal state of the builder.
     *
     * @return the built domain
     * @throws IllegalStateException if there was a problem reading the template domains
     */
    public VariableDomain toDomain() {
        String domainName = calculateDomainFilename();
        String domain;
        try {
            domain = IOUtils.concatReadAllLines(getClass().getResourceAsStream(domainName));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file \"" + domainName + "\" for creating a domain.", e);
        }
        // TODO: metric and goal text
        return domainIO.parse(domain);
    }

    /**
     * Calculate the template domain filename from the internal state.
     *
     * @return the calculated filename
     */
    private String calculateDomainFilename() {
        char separator = '/';
        StringBuilder builder = new StringBuilder("domain-variants").append(separator);
        builder.append(domainType.get().toString().toLowerCase());
        builder.append(separator).append("domain").append(separator).append("domain-");

        builder.append(isCapacity() ? "" : "No").append("Cap");
        builder.append("-");
        builder.append(isFuel() ? "" : "No").append("Fuel");
        builder.append("-");
        builder.append(isNumeric() ? "" : "No").append("Num");

        builder.append(".pddl");
        return builder.toString();
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name.get();
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Get the name property.
     *
     * @return the name property
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Get the domain type.
     *
     * @return the domain type
     */
    public DomainType getDomainType() {
        return domainType.get();
    }

    /**
     * Sets the domain type.
     *
     * @param domainType the domain type
     */
    public void setDomainType(DomainType domainType) {
        this.domainType.set(domainType);
    }

    /**
     * Get the domainType property.
     *
     * @return the domainType property
     */
    public ObjectProperty<DomainType> domainTypeProperty() {
        return domainType;
    }

    /**
     * True iff the domain to be built is fuel-enabled.
     *
     * @return is fuel enabled
     */
    public boolean isFuel() {
        return fuel.get();
    }

    /**
     * Sets the domain to be fuel-enabled.
     *
     * @param fuel the fuel
     */
    public void setFuel(boolean fuel) {
        this.fuel.set(fuel);
    }

    /**
     * Get the fuel property.
     *
     * @return the fuel property
     */
    public BooleanProperty fuelProperty() {
        return fuel;
    }

    /**
     * True iff the domain to be built is capacity-enabled.
     *
     * @return is capacity enabled
     */
    public boolean isCapacity() {
        return capacity.get();
    }

    /**
     * Sets the domain to be capacity-enabled.
     *
     * @param capacity the capacity
     */
    public void setCapacity(boolean capacity) {
        this.capacity.set(capacity);
    }

    /**
     * Get the capacity property.
     *
     * @return the capacity property
     */
    public BooleanProperty capacityProperty() {
        return capacity;
    }

    /**
     * True iff the domain to be built is numeric.
     *
     * @return is numeric
     */
    public boolean isNumeric() {
        return numeric.get();
    }

    /**
     * Sets the domain to be numeric.
     *
     * @param numeric the numeric
     */
    public void setNumeric(boolean numeric) {
        this.numeric.set(numeric);
    }

    /**
     * Get the numeric property.
     *
     * @return the numeric property
     */
    public BooleanProperty numericProperty() {
        return numeric;
    }

    /**
     * Get the goal text.
     *
     * @return the goal text
     */
    public String getGoalText() {
        return goalText.get();
    }

    /**
     * Sets the goal text.
     *
     * @param goalText the goal text
     */
    public void setGoalText(String goalText) {
        this.goalText.set(goalText);
    }

    /**
     * Get the goalText property.
     *
     * @return the goalText property
     */
    public StringProperty goalTextProperty() {
        return goalText;
    }

    /**
     * Get the metric text.
     *
     * @return the metric text
     */
    public String getMetricText() {
        return metricText.get();
    }

    /**
     * Sets the metric text.
     *
     * @param metricText the metric text
     */
    public void setMetricText(String metricText) {
        this.metricText.set(metricText);
    }

    /**
     * Get the metricText property.
     *
     * @return the metricText property
     */
    public StringProperty metricTextProperty() {
        return metricText;
    }
}
