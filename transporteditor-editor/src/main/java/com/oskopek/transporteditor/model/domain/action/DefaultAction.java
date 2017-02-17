package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

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
        return new HashCodeBuilder(17, 37).append(getName()).append(getWho()).append(getWhere()).append(getWhat())
                .append(getPreconditions()).append(getEffects()).append(getCost()).append(getDuration()).toHashCode();
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
        return new EqualsBuilder().append(getName(), that.getName()).append(getWho(), that.getWho()).append(getWhere(),
                that.getWhere()).append(getWhat(), that.getWhat()).append(getPreconditions(), that.getPreconditions())
                .append(getEffects(), that.getEffects()).append(getCost(), that.getCost()).append(getDuration(),
                        that.getDuration()).isEquals();
    }

    @Override
    public String toString() {
        return name + "[" + who + " @ " + where + " -> " + what + "]";
    }
}
