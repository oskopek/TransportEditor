package com.oskopek.transport.model.domain.actionbuilder;

import com.oskopek.transport.model.domain.action.DefaultAction;
import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Locatable;
import com.oskopek.transport.model.problem.Location;

import java.util.List;

/**
 * A mutable template for building actions of a given type when supplied the correct arguments.
 *
 * @param <Action_> the resulting action type
 * @param <Who_> the who type
 * @param <What_> the what type
 */
public interface ActionBuilder<Action_ extends DefaultAction<Who_, What_>, Who_ extends Locatable, What_ extends
        ActionObject> {

    /**
     * Build the action from the supplied arguments according to this template.
     *
     * @param who the who argument
     * @param where the where argument
     * @param what the what argument
     * @param <Who__> the exact who type
     * @param <What__> the exact what type
     * @return the built action
     */
    <Who__ extends Who_, What__ extends What_> Action_ build(Who__ who, Location where, What__ what);

    /**
     * Get the preconditions.
     *
     * @return the preconditions
     */
    List<Predicate> getPreconditions();

    /**
     * Get the effects.
     *
     * @return the effects
     */
    List<Predicate> getEffects();

}
