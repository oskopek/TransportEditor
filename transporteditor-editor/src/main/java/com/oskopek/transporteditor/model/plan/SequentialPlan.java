package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public final class SequentialPlan implements Plan {

    private final LinkedList<Action> actionList;

    public SequentialPlan(LinkedList<Action> actionList) {
        this.actionList = actionList;
    }

    public SequentialPlan(List<Action> actionList) {
        this.actionList = new LinkedList<>(actionList);
    }

    @Override
    public List<Action> getActions() {
        return actionList;
    }

    @Override
    public Set<TemporalPlanAction> getTemporalPlanActions() {
        return new HashSet<>(getTemporalPlanActionsList());
    }

    public List<TemporalPlanAction> getTemporalPlanActionsList() {
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
