package com.oskopek.transporteditor.model.domain;

import com.oskopek.transporteditor.model.domain.action.Drive;
import com.oskopek.transporteditor.model.domain.action.Drop;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.domain.action.Refuel;
import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;

import java.util.Map;
import java.util.Set;

/**
 * Represents the actual specific Transport domain, with all the constraints, parameters and objective functions
 * fully specified.
 * <p>
 * The domain also knows how to export itself (with all the technicalities) as a valid PDDL file and export (valid)
 * plans created in this domain.
 */
public interface Domain {

    /**
     * Get the name of the domain.
     *
     * @return the name
     */
    String getName();

    /**
     * Get the predicate map.
     *
     * @return the predicate map
     */
    Map<String, Class<? extends Predicate>> getPredicateMap();

    /**
     * Get the function map.
     *
     * @return the function map
     */
    Map<String, Class<? extends Function>> getFunctionMap();

    /**
     * Get the drive builder.
     *
     * @return the drive builder
     */
    DriveBuilder getDriveBuilder();

    /**
     * Get the drop builder.
     *
     * @return the drop builder
     */
    DropBuilder getDropBuilder();

    /**
     * Get the pick-up builder.
     *
     * @return the pick-up builder
     */
    PickUpBuilder getPickUpBuilder();

    /**
     * Get the refuel builder.
     *
     * @return the refuel builder
     */
    RefuelBuilder getRefuelBuilder();

    /**
     * Build the drive action with the associated drive builder.
     *
     * @param vehicle the vehicle
     * @param from the from location
     * @param to the to location
     * @param road the road to use
     * @return the built action
     */
    Drive buildDrive(Vehicle vehicle, Location from, Location to, Road road);

    /**
     * Build the drive action with the associated drive builder.
     *
     * @param vehicle the vehicle
     * @param from the from location
     * @param to the to location
     * @param graph the graph to use for finding the shortest edge
     * @return the built action
     */
    Drive buildDrive(Vehicle vehicle, Location from, Location to, RoadGraph graph);

    /**
     * Build the drop action with the associated drop builder.
     *
     * @param vehicle the vehicle
     * @param at the location
     * @param what the package to drop
     * @return the built action
     */
    Drop buildDrop(Vehicle vehicle, Location at, Package what);

    /**
     * Build the pick-up action with the associated pick-up builder.
     *
     * @param vehicle the vehicle
     * @param at the location
     * @param what the package to pick up
     * @return the built action
     */
    PickUp buildPickUp(Vehicle vehicle, Location at, Package what);

    /**
     * Build the refuel action with the associated refuel builder.
     *
     * @param vehicle the vehicle
     * @param at the location
     * @return the built action
     */
    Refuel buildRefuel(Vehicle vehicle, Location at);

    /**
     * Get the PDDL labels.
     *
     * @return the PDDL labels
     */
    Set<PddlLabel> getPddlLabels();

}
