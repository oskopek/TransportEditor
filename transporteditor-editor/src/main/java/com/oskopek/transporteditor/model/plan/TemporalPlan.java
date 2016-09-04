/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.plan;

import com.github.kevinjdolan.intervaltree.Interval;
import com.github.kevinjdolan.intervaltree.IntervalTree;
import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TemporalPlan implements Plan, Iterable<TemporalPlanAction> {

    private final IntervalTree<Action> actionIntervalTree = new IntervalTree<>();

    public TemporalPlan(Collection<TemporalPlanAction> planActions) {
        planActions.forEach(
                t -> actionIntervalTree.addInterval(t.getStartTimestamp(), t.getEndTimestamp(), t.getAction()));
        actionIntervalTree.build();
    }

    public Set<Action> getActionsAt(Integer timestamp) {
        return actionIntervalTree.get(timestamp);
    }

    @Override
    public Set<TemporalPlanAction> getTemporalPlanActions() {
        return actionIntervalTree.getIntervals(Integer.MIN_VALUE, Integer.MAX_VALUE).stream().map(
                i -> new TemporalPlanAction(i.getData(), (int) i.getStart(), (int) i.getEnd())).collect(
                Collectors.toSet());
    }

    @Override
    public Collection<Action> getAllActions() {
        return actionIntervalTree.getIntervals(Integer.MIN_VALUE, Integer.MAX_VALUE).stream().map(Interval::getData)
                .collect(Collectors.toSet());
    }

    @Override
    public Iterator<TemporalPlanAction> iterator() {
        return getTemporalPlanActions().iterator();
    }

    @Override
    public void forEach(Consumer<? super TemporalPlanAction> action) {
        getTemporalPlanActions().forEach(action);
    }

    @Override
    public Spliterator<TemporalPlanAction> spliterator() {
        return getTemporalPlanActions().spliterator();
    }
}
