package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Chooses a package randomly and sample a nearby vehicle from a distribution based on inverse distance to the package,
 * find the path to deliver the package by the vehicle,
 * find all packages on it and around it
 * choose the ones whose target is on the path
 * if still not fully capacitated, choose the others in the
 * order of distance from their destinations at any possible drop of point on the path.
 */
public class RandomizedRestartWithAroundPathPickupNoCoinTossPlanner extends SequentialRandomizedPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor.
     */
    public RandomizedRestartWithAroundPathPickupNoCoinTossPlanner() {
        setName(RandomizedRestartWithAroundPathPickupNoCoinTossPlanner.class.getSimpleName());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(problem);
        logger.debug("Starting planning...");
        double temperature = 1d; // TODO test this out
        while (true) {
            ImmutablePlanState current = new ImmutablePlanState(problem);
            while (!current.isGoalState() && current.getTotalTime() < getBestPlanScore()) {
                Problem curProblem = current.getProblem();
                List<Package> unfinished = new ArrayList<>(
                        PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                if (unfinished.isEmpty()) {
                    throw new IllegalStateException("Zero packages left but not in goal state.");
                }

                Package chosenPackage = unfinished.get(getRandom().nextInt(unfinished.size()));
                Vehicle chosenVehicle;
                while (true) {
                    chosenVehicle = chooseFromDistanceDistribution(curProblem.getAllVehicles(),
                            chosenPackage.getLocation(), chosenPackage.getSize().getCost(), temperature);
                    if (chosenVehicle.getCurCapacity().getCost() >= chosenPackage.getSize().getCost()) {
                        break;
                    }
                }

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage,
                        unfinished, true);
                current = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to state."));

                if (shouldCancel()) {
                    logger.debug("Cancelling, returning best found plan so far with score: {}.", getBestPlanScore());
                    return Optional.ofNullable(getBestPlan());
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Finished one iteration. Length: {}", current.getTotalTime());
            }

            // TODO: collapse plan?
            if (getBestPlanScore() > current.getTotalTime()) {
                logger.debug("Found new best plan {} -> {}", getBestPlanScore(), current.getTotalTime());
                setBestPlanScore(current.getTotalTime());
                setBestPlan(new SequentialPlan(current.getAllActionsInList()));
            }
        }
    }

    /**
     * Choose the vehicle based on the distribution created by measuring inverse distances to a given location.
     * Only considers vehicles that have a certain free capacity.
     *
     * @param vehicles the vehicles to choose from
     * @param to the location to which we are measuring the distances
     * @param minFreeCapacity the minimum capacity vehicles must have
     * @param temperature evens out the distribution. For temperatures larger than 1, the probability of picking
     * more distant vehicles rises. The neutral value is 1,
     * values less than 1 make the distribution prefer the nearest vehicles.
     * @return a vehicle randomly sampled from the created distribution
     */
    private Vehicle chooseFromDistanceDistribution(Collection<Vehicle> vehicles, Location to, int minFreeCapacity,
            double temperature) {
        List<Tuple2<Double, Vehicle>> vehDistances = vehicles.stream()
                .filter(v -> v.getCurCapacity().getCost() >= minFreeCapacity)
                .sorted(Comparator.comparing(Vehicle::getName))
                .map(v -> Tuple.of((double) getShortestPathMatrix().get(v.getLocation().getName(), to.getName())
                        .getDistance(), v))
                .collect(Collectors.toList());
        vehDistances = vehDistances.stream().map(t -> Tuple.of((1 / (t._1 + 1)) * temperature, t._2))
                .collect(Collectors.toList());
        double sumOfDistances = vehDistances.stream().mapToDouble(t -> t._1).sum();
        List<Double> vehProbs = vehDistances.stream()
                .map(t -> t._1 / sumOfDistances).collect(Collectors.toList());
        double intermediateSum = 0d;
        for (int i = 0; i < vehProbs.size(); i++) {
            Double elem = vehProbs.get(i);
            intermediateSum += elem;
            vehProbs.set(i, intermediateSum);
        }

        double rand = getRandom().nextDouble();
        int chosenIndex = 0;
        while (chosenIndex < vehProbs.size() && rand > vehProbs.get(chosenIndex)) {
            chosenIndex++;
        }
        return vehDistances.get(chosenIndex)._2;
    }

    @Override
    public RandomizedRestartWithAroundPathPickupNoCoinTossPlanner copy() {
        return new RandomizedRestartWithAroundPathPickupNoCoinTossPlanner();
    }
}
