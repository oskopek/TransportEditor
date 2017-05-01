package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.action.*;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reader and writer for {@link SequentialPlan} to and from the VAL format (supports only Transport domain plans).
 * Uses regexps for parsing.
 */
public class SequentialPlanIO implements DataIO<Plan> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Problem problem;
    private final Domain domain;

    /**
     * Default constructor.
     *
     * @param domain the associated domain
     * @param problem the associated problem
     */
    public SequentialPlanIO(Domain domain, Problem problem) {
        this.domain = domain;
        this.problem = problem;
    }

    /**
     * Util method for serializing an action to a VAL-format plan line. Does not handle time or capacities.
     *
     * @param action the action to serialize
     * @return a string builder containing the serialized action (without a closing parenthesis)
     */
    static StringBuilder serializeActionSimple(Action action) {
        StringBuilder str = new StringBuilder();
        String whoName = action.getWho().getName();
        str.append('(').append(action.getName()).append(' ').append(whoName).append(' ')
                .append(action.getWhere().getName());
        if (Drive.class.isInstance(action)) {
            str.append(' ').append(action.getWhat().getName());
        } else if (PickUp.class.isInstance(action)) {
            str.append(' ').append(action.getWhat().getName());
        } else if (Drop.class.isInstance(action)) {
            str.append(' ').append(action.getWhat().getName());
        } else if (Refuel.class.isInstance(action)) {
            str.append(""); // intentionally empty
        } else {
            throw new IllegalArgumentException("Not recognized action: " + action);
        }
        return str;
    }

    /**
     * Util method for serializing an action to a VAL-like format plan line. Does not handle time or capacities.
     * Is only <strong>informative</strong>.
     *
     * @param action the action to serialize
     * @return a string containing the approximately serialized action
     */
    public static String toApproximateValFormat(Action action) {
        return serializeActionSimple(action).append(')').toString();
    }

    /**
     * Util method for parsing an action from a VAL-format plan line. Does not handle time.
     *
     * @param domain the domain to parse to
     * @param problem the problem to parse to
     * @param line the line to parse
     * @return the parsed action
     * @throws IllegalArgumentException if an error during parsing occurs
     * @throws IllegalStateException if a refuel action occurs in a fuel-less domain
     */
    static Action parsePlanAction(Domain domain, Problem problem, String line) {
        Pattern actionPattern = Pattern.compile("\\((([-a-zA-Z0-9]+ )+([-a-zA-Z0-9]+))\\)");
        Matcher matcher = actionPattern.matcher(line);

        String inside;
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
                if (!domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                    throw new IllegalStateException("Cannot have a refuel action in a domain without fuel.");
                }
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

    /**
     * Internal capacity-aware action serialization.
     *
     * @param action the action to serialize
     * @param curCapacity the current capacity of the current (who) vehicle
     * @return the serialized VAL-format plan action line
     */
    private static String serializeAction(Action action, int curCapacity) {
        StringBuilder str = serializeActionSimple(action);
        if (PickUp.class.isInstance(action)) {
            str.append(' ').append("capacity-").append(curCapacity - 1).append(' ').append("capacity-")
                    .append(curCapacity);
        } else if (Drop.class.isInstance(action)) {
            str.append(' ').append("capacity-").append(curCapacity).append(' ').append("capacity-")
                    .append(curCapacity + 1);
        }
        str.append(')');
        return str.toString();
    }

    @Override
    public synchronized String serialize(Plan plan) {
        if (plan == null) {
            return null;
        }
        Map<String, Integer> capacityMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        for (Action action : plan.getActions()) {
            Vehicle vehicle = (Vehicle) action.getWho();
            int capacity = capacityMap.computeIfAbsent(vehicle.getName(), v -> vehicle.getCurCapacity().getCost());
            builder.append(serializeAction(action, capacity)).append('\n');

            if (action instanceof PickUp) {
                capacity--;
                if (capacity < 0) {
                    logger.warn("Cur capacity cannot be less than zero capacity.");
                }
            } else if (action instanceof Drop) {
                capacity++;
                if (capacity > vehicle.getMaxCapacity().getCost()) {
                    logger.warn("Cur capacity cannot be bigger than max capacity.");
                }
            }
            capacityMap.put(vehicle.getName(), capacity);
        }
        ActionCost totalCost = plan.getActions().stream().map(Action::getCost).reduce(ActionCost.ZERO,
                ActionCost::add);
        if (totalCost != null) {
            builder.append("; cost = ").append(totalCost.getCost()).append(" (general cost)");
        }
        return builder.toString();
    }

    @Override
    public SequentialPlan parse(String contents) {
        List<Action> actions = Arrays.stream(contents.split("\n")).map(s -> {
            int index = s.indexOf(';');
            return index >= 0 ? s.substring(0, index) : s;
        }).filter(s -> !s.isEmpty()).map(s -> SequentialPlanIO.parsePlanAction(domain, problem, s)).collect(
                Collectors.toList());
        return new SequentialPlan(actions);
    }
}
