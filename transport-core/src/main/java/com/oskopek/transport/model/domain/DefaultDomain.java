package com.oskopek.transport.model.domain;

import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.domain.action.Drop;
import com.oskopek.transport.model.domain.action.PickUp;
import com.oskopek.transport.model.domain.action.Refuel;
import com.oskopek.transport.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transport.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transport.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transport.model.domain.actionbuilder.RefuelBuilder;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * Simple immutable abstract data-holding domain implementation. Provides equality checking.
 */
public abstract class DefaultDomain implements Domain {

    private final String name;

    private final DriveBuilder driveBuilder;
    private final DropBuilder dropBuilder;
    private final PickUpBuilder pickUpBuilder;
    private final RefuelBuilder refuelBuilder;

    private final Set<PddlLabel> pddlLabelSet;

    /**
     * Default constructor.
     *
     * @param name the name
     * @param driveBuilder the drive builder
     * @param dropBuilder the drop builder
     * @param pickUpBuilder the pick-up builder
     * @param refuelBuilder the refuel builder
     * @param pddlLabelSet the pddl labels
     */
    public DefaultDomain(String name, DriveBuilder driveBuilder, DropBuilder dropBuilder, PickUpBuilder pickUpBuilder,
            RefuelBuilder refuelBuilder, Set<PddlLabel> pddlLabelSet) {
        this.name = name;
        this.driveBuilder = driveBuilder;
        this.dropBuilder = dropBuilder;
        this.pickUpBuilder = pickUpBuilder;
        this.refuelBuilder = refuelBuilder;
        this.pddlLabelSet = pddlLabelSet;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DriveBuilder getDriveBuilder() {
        return driveBuilder;
    }

    @Override
    public DropBuilder getDropBuilder() {
        return dropBuilder;
    }

    @Override
    public PickUpBuilder getPickUpBuilder() {
        return pickUpBuilder;
    }

    @Override
    public RefuelBuilder getRefuelBuilder() {
        return refuelBuilder;
    }

    @Override
    public Drive buildDrive(Vehicle vehicle, Location from, Location to, Road road) {
        return driveBuilder.build(vehicle, from, to, road, pddlLabelSet.contains(PddlLabel.Fuel));
    }

    @Override
    @Deprecated
    public Drive buildDrive(Vehicle vehicle, Location from, Location to, RoadGraph graph) {
        return driveBuilder.build(vehicle, from, to, graph, pddlLabelSet.contains(PddlLabel.Fuel));
    }

    @Override
    public Drop buildDrop(Vehicle vehicle, Location at, Package what) {
        return dropBuilder.build(vehicle, at, what);
    }

    @Override
    public PickUp buildPickUp(Vehicle vehicle, Location at, Package what) {
        return pickUpBuilder.build(vehicle, at, what);
    }

    @Override
    public Refuel buildRefuel(Vehicle vehicle, Location at) {
        return refuelBuilder.build(vehicle, at);
    }

    @Override
    public Set<PddlLabel> getPddlLabels() {
        return pddlLabelSet;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(driveBuilder)
                .append(dropBuilder)
                .append(pickUpBuilder)
                .append(refuelBuilder)
                .append(pddlLabelSet)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultDomain)) {
            return false;
        }
        DefaultDomain that = (DefaultDomain) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(driveBuilder, that.driveBuilder)
                .append(dropBuilder, that.dropBuilder)
                .append(pickUpBuilder, that.pickUpBuilder)
                .append(refuelBuilder, that.refuelBuilder)
                .append(pddlLabelSet, that.pddlLabelSet)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("pddlLabelSet", pddlLabelSet)
                .append("pddlLabels", pddlLabelSet).append("functionMap", getFunctionMap())
                .append("predicateMap", getPredicateMap()).toString();
    }
}
