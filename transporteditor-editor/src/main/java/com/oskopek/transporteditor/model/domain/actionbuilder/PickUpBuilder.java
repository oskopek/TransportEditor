package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

/**
 * Builder for the drive action.
 */
public class PickUpBuilder extends DefaultActionBuilderWithCost<PickUp, Vehicle, Package> {

    /**
     * Default constructor.
     *
     * @param preconditions the preconditions
     * @param effects the effects
     * @param cost the cost
     * @param duration the duration
     */
    public PickUpBuilder(List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super(preconditions, effects, cost, duration);
    }

    @Override
    public <Who_ extends Vehicle, What_ extends Package> PickUp build(Who_ who, Location where, What_ what) {
        return new PickUp(who, where, what, getPreconditions(), getEffects(), getCost(), getDuration());
    }
}
