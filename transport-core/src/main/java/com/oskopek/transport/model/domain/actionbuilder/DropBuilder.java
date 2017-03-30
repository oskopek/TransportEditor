package com.oskopek.transport.model.domain.actionbuilder;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.domain.action.Drop;
import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;

import java.util.List;

/**
 * Builder for the drive action.
 */
public class DropBuilder extends DefaultActionBuilderWithCost<Drop, Vehicle, Package> {

    /**
     * Default constructor.
     *
     * @param preconditions the preconditions
     * @param effects the effects
     * @param cost the cost
     * @param duration the duration
     */
    public DropBuilder(List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super(preconditions, effects, cost, duration);
    }

    @Override
    public <Who_ extends Vehicle, What_ extends Package> Drop build(Who_ who, Location where, What_ what) {
        return new Drop(who, where, what, getPreconditions(), getEffects(), getCost(), getDuration());
    }
}
