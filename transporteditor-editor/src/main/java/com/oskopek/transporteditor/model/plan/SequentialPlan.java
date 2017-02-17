package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * A sequential domain's plan implementation. All actions are assumed to be non-intersecting and ordered.
 */
public final class SequentialPlan implements Plan {

    private final LinkedList<Action> actionList;

    /**
     * Default constructor.
     *
     * @param actionList the action list
     */
    private SequentialPlan(LinkedList<Action> actionList) {
        this.actionList = actionList;
    }

    /**
     * Default constructor that shallowly copies the given action list.
     *
     * @param actionList the action list
     */
    public SequentialPlan(List<Action> actionList) {
        this(new LinkedList<>(actionList));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns actions in the sequential order given at input.
     */
    @Override
    public List<Action> getActions() {
        return actionList;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Wraps actions into {@link TemporalPlanAction}s based on their order and duration. Time starts at 0.
     * The actions are guaranteed to be in the order specified by {@link #getActions()} and non-intersecting.
     */
    @Override
    public List<TemporalPlanAction> getTemporalPlanActions() {
        List<TemporalPlanAction> temporalActions = new ArrayList<>();
        int i = 0;
        for (Action action : actionList) {
            int begin = i;
            if (action.getDuration() == null) {
                throw new IllegalStateException("Action duration cannot be null: " + action);
            }
            i += action.getDuration().getCost();
            temporalActions.add(new TemporalPlanAction(action, begin, i));
        }
        return temporalActions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(actionList).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SequentialPlan)) {
            return false;
        }
        SequentialPlan actions = (SequentialPlan) o;
        return new EqualsBuilder().append(actionList, actions.actionList).isEquals();
    }
}
