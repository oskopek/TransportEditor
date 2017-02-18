package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.DefaultAction;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Adds cost and duration to the {@link DefaultActionBuilder}.
 *
 * @param <Who> the who type
 * @param <Where> the where type
 * @param <What> the what type
 */
public abstract class DefaultActionBuilderWithCost<Who extends DefaultAction<Where, What>, Where extends Locatable,
        What extends ActionObject> extends DefaultActionBuilder<Who, Where, What> {

    private final ActionCost cost;
    private final ActionCost duration;

    /**
     * Default constructor.
     *
     * @param preconditions the preconditions
     * @param effects the effects
     * @param cost the cost
     * @param duration the duration
     */
    public DefaultActionBuilderWithCost(List<Predicate> preconditions, List<Predicate> effects, ActionCost cost,
            ActionCost duration) {
        super(preconditions, effects);
        this.cost = cost;
        this.duration = duration;
    }

    /**
     * Get the cost.
     *
     * @return the cost
     */
    public ActionCost getCost() {
        return cost;
    }

    /**
     * Get the duration.
     *
     * @return the duration
     */
    public ActionCost getDuration() {
        return duration;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getCost())
                .append(getDuration())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultActionBuilderWithCost)) {
            return super.equals(o);
        }
        DefaultActionBuilderWithCost<?, ?, ?> that = (DefaultActionBuilderWithCost<?, ?, ?>) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(getCost(), that.getCost())
                .append(getDuration(), that.getDuration())
                .isEquals();
    }
}
