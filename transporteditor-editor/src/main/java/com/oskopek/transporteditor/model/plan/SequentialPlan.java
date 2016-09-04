/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;

import java.util.*;
import java.util.function.Consumer;

public final class SequentialPlan implements Iterable<Action>, Plan {

    private final LinkedList<Action> actionList;

    public SequentialPlan(LinkedList<Action> actionList) {
        this.actionList = actionList;
    }

    @Override
    public List<Action> getAllActions() {
        return actionList;
    }

    @Override
    public Set<TemporalPlanAction> getTemporalPlanActions() {
        Set<TemporalPlanAction> set = new HashSet<>();
        int i = 0;
        for (Action action : actionList) {
            int begin = i;
            i += action.getDuration().getCost();
            set.add(new TemporalPlanAction(action, begin, i));
        }
        return set;
    }

    @Override
    public Iterator<Action> iterator() {
        return actionList.iterator();
    }

    @Override
    public void forEach(Consumer<? super Action> action) {
        actionList.forEach(action);
    }

    @Override
    public Spliterator<Action> spliterator() {
        return actionList.spliterator();
    }
}
