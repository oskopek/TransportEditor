package com.oskopek.transport.model.domain.actionbuilder;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.domain.action.Refuel;
import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.PlaceholderActionObject;
import com.oskopek.transport.model.problem.Vehicle;

import java.util.List;

/**
 * Builder for the refuel action.
 */
public class RefuelBuilder extends DefaultActionBuilderWithCost<Refuel, Vehicle, PlaceholderActionObject> {

    /**
     * Default constructor.
     *
     * @param preconditions the preconditions
     * @param effects the effects
     * @param cost the cost
     * @param duration the duration
     */
    public RefuelBuilder(List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super(preconditions, effects, cost, duration);
    }

    @Override
    public <Who_ extends Vehicle, What_ extends PlaceholderActionObject> Refuel build(Who_ who, Location where,
            What_ what) {
        return build(who, where);
    }

    /**
     * A {@link #build(Vehicle, Location, PlaceholderActionObject)} wrapper that correctly ignores the what argument.
     *
     * @param who the vehicle
     * @param where the location
     * @param <Who_> the exacty who type
     * @return the built refuel action
     */
    public <Who_ extends Vehicle> Refuel build(Who_ who, Location where) {
        return new Refuel(who, where, getPreconditions(), getEffects(), getCost(), getDuration());
    }
}
