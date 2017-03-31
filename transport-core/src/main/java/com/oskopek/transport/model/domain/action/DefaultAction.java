package com.oskopek.transport.model.domain.action;

import com.oskopek.transport.model.domain.action.predicates.Predicate;
import com.oskopek.transport.model.problem.ActionObject;
import com.oskopek.transport.model.problem.Locatable;
import com.oskopek.transport.model.problem.Location;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * A data-wrapping abstract action. Overrides equals and hashCode.
 *
 * @param <Who> the who type
 * @param <What> the what type
 */
public abstract class DefaultAction<Who extends Locatable, What extends ActionObject> implements Action {

    private final String name;
    private final Who who;
    private final Location where;
    private final What what;
    private final List<Predicate> preconditions;
    private final List<Predicate> effects;
    private final ActionCost cost;
    private final ActionCost duration;

    /**
     * Default constructor.
     *
     * @param name the name
     * @param who the who
     * @param where the where
     * @param what the what
     * @param preconditions the preconditions
     * @param effects the effects
     * @param cost the cost
     * @param duration the duration
     */
    public DefaultAction(String name, Who who, Location where, What what, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        this.name = name;
        this.who = who;
        this.where = where;
        this.what = what;
        this.preconditions = preconditions;
        this.effects = effects;
        this.cost = cost;
        this.duration = duration;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Who getWho() {
        return who;
    }

    @Override
    public Location getWhere() {
        return where;
    }

    @Override
    public What getWhat() {
        return what;
    }

    @Override
    public List<Predicate> getPreconditions() {
        return preconditions;
    }

    @Override
    public List<Predicate> getEffects() {
        return effects;
    }

    @Override
    public ActionCost getCost() {
        return cost;
    }

    @Override
    public ActionCost getDuration() {
        return duration;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(who).append(where).append(what)
                .append(preconditions).append(effects).append(cost).append(duration).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultAction)) {
            return false;
        }
        DefaultAction<?, ?> that = (DefaultAction<?, ?>) o;
        return new EqualsBuilder().append(name, that.name).append(who, that.who).append(where,
                that.where).append(what, that.what).append(preconditions, that.preconditions)
                .append(effects, that.effects).append(cost, that.cost).append(duration,
                        that.duration).isEquals();
    }

    @Override
    public String toString() {
        return name + '[' + Optional.ofNullable(who).map(ActionObject::getName).orElse("null") + " @ "
                + Optional.ofNullable(where).map(ActionObject::getName).orElse("null")
                + " -> " + Optional.ofNullable(what).map(ActionObject::getName).orElse("null") + ']';
    }
}
