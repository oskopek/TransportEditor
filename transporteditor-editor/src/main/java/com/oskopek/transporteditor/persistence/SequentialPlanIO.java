/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.action.*;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.DefaultProblem;

public class SequentialPlanIO implements DataReader<SequentialPlan>, DataWriter<SequentialPlan> {

    private final DefaultProblem problem;

    public SequentialPlanIO(DefaultProblem problem) {
        this.problem = problem;
    }

    public static String serializeAction(Action action) {
        StringBuilder str = new StringBuilder();
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
    public String serialize(SequentialPlan plan) throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();
        plan.forEach(action -> builder.append(serializeAction(action)).append('\n'));
        ActionCost totalCost = plan.getAllActions().stream().map(Action::getCost).reduce(ActionCost.valueOf(0),
                ActionCost::add);
        if (totalCost != null) {
            builder.append("; cost = ").append(totalCost.getCost()).append(" (general-cost)\n");
        }
        return builder.toString();
    }

    @Override
    public SequentialPlan parse(String contents) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
