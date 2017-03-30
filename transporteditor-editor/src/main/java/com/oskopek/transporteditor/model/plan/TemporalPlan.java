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

/**
 * A temporal domain's plan implementation. All actions are assumed to be potentially intersecting and unordered.
 * Implementation is backed by a
 * <a href="https://en.wikipedia.org/wiki/Interval_tree#Centered_interval_tree">centered interval tree</a>.
 */
public class TemporalPlan implements Plan {

    private final IntervalTree<TemporalPlanAction> actionIntervalTree = IntervalTree.empty();

    // still allows for plans of about 100_000_000_000 duration
    private static final long DISCRETIZATION_CONSTANT = 10_000_000L;

    /**
     * Default constructor. Blocks while building the interval tree.
     *
     * @param planActions the actions to add
     */
    public TemporalPlan(Collection<TemporalPlanAction> planActions) {
        planActions.forEach(t -> actionIntervalTree.addInterval(
                Math.round(DISCRETIZATION_CONSTANT * t.getStartTimestamp()),
                Math.round(DISCRETIZATION_CONSTANT * t.getEndTimestamp()), t));
        actionIntervalTree.build();
    }

    /**
     * Get actions occurring at given time.
     *
     * @param timestamp the timestamp to query at
     * @return a set of actions occurring at the given time
     */
    public Set<Action> getActionsAt(double timestamp) {
        return getTemporalActionsAt(timestamp).stream().map(TemporalPlanAction::getAction).collect(Collectors.toSet());
    }

    /**
     * Get actions occurring at given time.
     *
     * @param timestamp the timestamp to query at
     * @return a set of actions occurring at the given time
     */
    public Set<Action> getActionsAt(int timestamp) {
        return getTemporalActionsAt((double) timestamp).stream().map(TemporalPlanAction::getAction)
                .collect(Collectors.toSet());
    }

    /**
     * Get temporal actions occurring at given time.
     *
     * @param timestamp the timestamp to query at
     * @return a set of temporal actions occurring at the given time
     */
    public Set<TemporalPlanAction> getTemporalActionsAt(double timestamp) {
        return actionIntervalTree.get(Math.round(timestamp * DISCRETIZATION_CONSTANT));
    }

    @Override
    public Set<Action> getActions() {
        return getTemporalPlanActions().stream().map(TemporalPlanAction::getAction).collect(Collectors.toSet());
    }

    @Override
    public Set<TemporalPlanAction> getTemporalPlanActions() {
        return actionIntervalTree.getIntervals(Long.MIN_VALUE, Long.MAX_VALUE).stream().map(Interval::getData)
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
