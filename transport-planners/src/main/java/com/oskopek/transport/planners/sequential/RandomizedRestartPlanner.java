package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Chooses a vehicle and location randomly,
 * pick up as many packages as possible at that location,
 * and deliver them along the shortest path possible (calculated as brute force permutations and evaluated
 * using the shortest path matrix).
 */
public class RandomizedRestartPlanner extends SequentialRandomizedPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor.
     */
    public RandomizedRestartPlanner() {
        setName(RandomizedRestartPlanner.class.getSimpleName());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(problem);
        logger.debug("Starting planning...");

        List<Vehicle> vehicles = new ArrayList<>(problem.getAllVehicles());
        List<Location> locations = problem.getRoadGraph().getAllLocations().collect(Collectors.toList());
        while (true) {
            ImmutablePlanState current = new ImmutablePlanState(problem);
            while (!current.isGoalState() && current.getTotalTime() < getBestPlanScore()) {
                Problem curProblem = current.getProblem();
                Set<Package> unfinished = PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages());
                if (unfinished.isEmpty()) {
                    throw new IllegalStateException("Zero packages left but not in goal state.");
                }

                Vehicle chosenVehicle = vehicles.get(getRandom().nextInt(vehicles.size()));
                List<Package> chosenPackages = new ArrayList<>();
                Location chosenLocation;
                while (true) {
                    chosenLocation = locations.get(getRandom().nextInt(locations.size()));
                    final Location iterLocation = chosenLocation;
                    Iterator<Package> pkgIter = unfinished.stream().filter(p -> p.getLocation() != null
                            && p.getLocation().getName().equals(iterLocation.getName())).iterator();
                    if (!pkgIter.hasNext()) {
                        break;
                    }

                    int curCapacity = chosenVehicle.getCurCapacity().getCost();
                    while (pkgIter.hasNext()) {
                        Package pkg = pkgIter.next();
                        curCapacity -= pkg.getSize().getCost();
                        if (curCapacity >= 0) {
                            chosenPackages.add(pkg);
                        }
                    }
                    if (!chosenPackages.isEmpty()) {
                        break;
                    }
                }

                List<Action> newActions = findPlan(domain, curProblem.getVehicle(chosenVehicle.getName()),
                        chosenPackages);
                current = javaslang.collection.Stream.ofAll(newActions).foldLeft(Optional.of(current),
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

            int totalTime = current.getTotalTime();
            if (getBestPlanScore() > totalTime) {
                logger.debug("Found new best plan {} -> {}", getBestPlanScore(), totalTime);
                savePlanIfBetter(totalTime, new SequentialPlan(current.getAllActionsInList()));
            }
        }
    }

    /**
     * Build the path for the chosen packages.
     *
     * @param domain the domain
     * @param chosenVehicle the chosen vehicle
     * @param chosenPackages the chosen packages
     * @return the plan
     */
    private List<Action> findPlan(Domain domain, Vehicle chosenVehicle, List<Package> chosenPackages) {
        List<Action> actions = new ArrayList<>();
        if (chosenPackages.isEmpty()) {
            return actions;
        }
        Location packageLoc = chosenPackages.get(0).getLocation();

        // drive to packages
        ShortestPath toPackages = getShortestPathMatrix().get(chosenVehicle.getLocation().getName(),
                chosenPackages.get(0).getLocation().getName());
        toPackages.getRoads().forEach(re -> actions.add(domain.buildDrive(chosenVehicle, re.getFrom(), re.getTo(),
                re.getRoad())));

        // pick packages up
        chosenPackages.forEach(p -> actions.add(domain.buildPickUp(chosenVehicle, p.getLocation(), p)));

        // drive to each target
        Stream.ofAll(chosenPackages).map(pkg -> pkg.getTarget().getName()).distinct().permutations()
                .map(Stream::toList)
                .map(lList -> lList.prepend(packageLoc.getName())).map(lList -> lList.zip(lList.toStream().drop(1)))
                .map(lTuples -> lTuples.map(lTuple -> getShortestPathMatrix().get(lTuple._1, lTuple._2)))
                .minBy(lPaths -> (long) lPaths.toStream().map(ShortestPath::getDistance).sum())
                .getOrElseThrow(() -> new IllegalStateException("Could not find the list of shortest paths."))
                .forEach(path -> {
                    path.getRoads().forEach(re ->
                            actions.add(domain.buildDrive(chosenVehicle, re.getFrom(), re.getTo(), re.getRoad())));
                    if (path.getRoads().isEmpty()) {
                        return;
                    }
                    for (Package pkg : chosenPackages) {
                        if (pkg.getTarget().getName().equals(path.lastLocation().getName())) {
                            actions.add(domain.buildDrop(chosenVehicle, pkg.getTarget(), pkg));
                        }

                    }
                });
        return actions;
    }

    @Override
    public RandomizedRestartPlanner copy() {
        return new RandomizedRestartPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RandomizedRestartPlanner;
    }
}
