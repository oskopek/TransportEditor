/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.*;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SequentialPlanIO implements DataReader<SequentialPlan>, DataWriter<SequentialPlan> {

    private final Problem problem;
    private final Domain domain;

    public SequentialPlanIO(Domain domain, Problem problem) {
        this.domain = domain;
        this.problem = problem;
    }

    public static String serializeAction(Action action) {
        StringBuilder str = new StringBuilder();
        str.append("(").append(action.getName()).append(" ").append(action.getWho().getName()).append(" ").append(
                action.getWhere().getName());
        if (Drive.class.isInstance(action)) {
            str.append(" ").append(action.getWhat().getName());
        } else if (PickUp.class.isInstance(action)) {
            str.append(" ").append(action.getWhat().getName()).append(" ").append("capacity-").append("").append(" ")
                    .append("capacity-").append("");
        } else if (Drop.class.isInstance(action)) {
            str.append(" ").append(action.getWhat().getName()).append(" ").append("capacity-").append("").append(" ")
                    .append("capacity-").append("");
        } else if (Refuel.class.isInstance(action)) {
            // intentionally empty
        } else {
            throw new IllegalArgumentException("Not recognized action: " + action);
        }
        str.append(")");
        return str.toString();
    }

    public static Action parsePlanAction(Domain domain, Problem problem, String line) {
        Pattern actionPattern = Pattern.compile("\\((([-a-zA-Z0-9]+ )+([-a-zA-Z0-9]+))\\)");
        Matcher matcher = actionPattern.matcher(line);

        String inside = null;
        if (matcher.find()) {
            inside = matcher.group(1);
        } else {
            throw new IllegalArgumentException("Couldn't parse line: " + line);
        }

        String[] groups = inside.split(" ");

        String actionName = groups[0];
        Vehicle vehicle = problem.getVehicle(groups[1]);
        Location where = problem.getRoadGraph().getLocation(groups[2]);
        switch (actionName) {
            case "drop": {
                Package what = problem.getPackage(groups[3]);
                return domain.buildDrop(vehicle, where, what);
            }
            case "pick-up": {
                Package what = problem.getPackage(groups[3]);
                return domain.buildPickUp(vehicle, where, what);
            }
            case "refuel": {
                return domain.buildRefuel(vehicle, where);
            }
            case "drive": {
                Location to = problem.getRoadGraph().getLocation(groups[3]);
                return domain.buildDrive(vehicle, where, to, problem.getRoadGraph());
            }
            default:
                throw new IllegalArgumentException("Unknown action name: " + actionName);
        }
    }

    @Override
    public String serialize(SequentialPlan plan) throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();
        plan.forEach(action -> builder.append(serializeAction(action)).append('\n'));
        ActionCost totalCost = plan.getAllActions().stream().map(Action::getCost).reduce(ActionCost.valueOf(0),
                ActionCost::add);
        if (totalCost != null) {
            builder.append("; cost = ").append(totalCost.getCost()).append(" (general cost)");
        }
        return builder.toString();
    }

    @Override
    public SequentialPlan parse(String contents) throws IllegalArgumentException {
        List<Action> actions = Arrays.stream(contents.split("\n")).map(s -> {
            int index = s.indexOf(';');
            return index >= 0 ? s.substring(0, index) : s;
        }).filter(s -> !s.isEmpty()).map(s -> SequentialPlanIO.parsePlanAction(domain, problem, s)).collect(
                Collectors.toList());
        return new SequentialPlan(actions);
    }
}
