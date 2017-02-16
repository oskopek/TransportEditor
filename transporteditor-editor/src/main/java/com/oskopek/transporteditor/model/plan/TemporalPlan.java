package com.oskopek.transporteditor.model.plan;

import com.github.kevinjdolan.intervaltree.Interval;
import com.github.kevinjdolan.intervaltree.IntervalTree;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class TemporalPlan implements Plan {

    private final IntervalTree<TemporalPlanAction> actionIntervalTree = IntervalTree.empty();

    public TemporalPlan(Collection<TemporalPlanAction> planActions) {
        planActions.forEach(
                t -> actionIntervalTree.addInterval(t.getStartTimestamp(), t.getEndTimestamp(), t));
        actionIntervalTree.build();
    }

    public Set<Action> getActionsAt(Integer timestamp) {
        return getTemporalActionsAt(timestamp).stream().map(TemporalPlanAction::getAction).collect(Collectors.toSet());
    }

    public Set<TemporalPlanAction> getTemporalActionsAt(Integer timestamp) {
        return actionIntervalTree.get(timestamp);
    }

    @Override
    public Collection<Action> getActions() {
        return getTemporalPlanActions().stream().map(TemporalPlanAction::getAction).collect(Collectors.toSet());
    }

    @Override
    public Set<TemporalPlanAction> getTemporalPlanActions() {
        return actionIntervalTree.getIntervals(Integer.MIN_VALUE, Integer.MAX_VALUE).stream().map(Interval::getData)
                .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(actionIntervalTree).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemporalPlan)) {
            return false;
        }
        TemporalPlan that = (TemporalPlan) o;
        return new EqualsBuilder().append(actionIntervalTree, that.actionIntervalTree).isEquals();
    }
}
