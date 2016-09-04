/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.action.*;
import com.oskopek.transporteditor.planning.plan.PlanEntry;
import com.oskopek.transporteditor.planning.plan.SequentialPlan;
import com.oskopek.transporteditor.planning.problem.DefaultProblem;

import java.util.ArrayDeque;
import java.util.Deque;

public class SequentialPlanIO implements DataReader<SequentialPlan>, DataWriter<SequentialPlan> {

    private final DefaultProblem problem;

    public SequentialPlanIO(DefaultProblem problem) {
        this.problem = problem;
    }

    @Override
    public String serialize(SequentialPlan plan) throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();
        Deque<PlanEntry> entries = new ArrayDeque<>(plan.firstEntries());
        while (!entries.isEmpty()) {
            PlanEntry top = entries.removeFirst();
            builder.append(serializePlanEntry(top)).append('\n');
            entries.addAll(top.nextPlanEntries());
        }
        ActionCost totalCost = plan.aggregatePlanEntries().stream().map(a -> a.getAction().getCost()).reduce(
                ActionCost.valueOf(0), ActionCost::add);
        if (totalCost != null) {
            builder.append("; cost = ").append(totalCost.getCost()).append(" (general-cost)\n");
        }
        return builder.toString();
    }

    private String serializePlanEntry(PlanEntry planEntry) {
        StringBuilder str = new StringBuilder();
        Action action = planEntry.getAction();
        str.append("(").append(action.getName()).append(" ").append(action.getWho().getName()).append(" ").append(
                action.getWhere().getName());
        if (Drive.class.isInstance(action)) {
            str.append(" ").append(action.getWhat());
        } else if (PickUp.class.isInstance(action)) {
            str.append(" ").append(action.getWhat()).append(" ").append("capacity-number ").append("").append(" ")
                    .append("capacity-number ").append("");
        } else if (Drop.class.isInstance(action)) {
            str.append(" ").append(action.getWhat()).append(" ").append("capacity-number ").append("").append(" ")
                    .append("capacity-number ").append("");
        } else if (Refuel.class.isInstance(action)) {
            // intentionally empty
        } else {
            throw new IllegalArgumentException("Not recognized action: " + action);
        }
        str.append(")").append("\n");
        return str.toString();
    }

    @Override
    public SequentialPlan parse(String contents) throws IllegalArgumentException {
        return null;
    }
}
