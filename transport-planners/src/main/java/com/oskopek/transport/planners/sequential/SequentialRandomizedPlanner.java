package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Value;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract superclass of randomized sequential planners.
 */
public abstract class SequentialRandomizedPlanner extends AbstractPlanner {

    private ArrayTable<String, String, ShortestPath> shortestPathMatrix;
    private Random random;
    private Plan bestPlan;
    private int bestPlanScore;

    /**
     * Get the precalculated shortest path lookup matrix.
     *
     * @return shortest path table
     */
    protected ArrayTable<String, String, ShortestPath> getShortestPathMatrix() {
        return shortestPathMatrix;
    }

    /**
     * Get the random instance for this planner.
     *
     * @return the random instance
     */
    protected Random getRandom() {
        return random;
    }

    /**
     * Get the best plan so far.
     *
     * @return the best plan
     */
    protected Plan getBestPlan() {
        return bestPlan;
    }

    /**
     * Get the best plan score so far.
     *
     * @return the best plan score
     */
    protected int getBestPlanScore() {
        return bestPlanScore;
    }

    /**
     * Save the plan if it has a lower score than the currently best found plan.
     *
     * @param score the score of the plan
     * @param plan the plan
     */
    protected void savePlanIfBetter(int score, Plan plan) {
        if (bestPlanScore > score) {
            formatLog("Found new best plan {} -> {}", bestPlanScore, score);
            bestPlanScore = score;
            if (plan instanceof SequentialPlan) {
                bestPlan = new SequentialPlan(plan.getActions());
            } else if (plan instanceof TemporalPlan) {
                bestPlan = new TemporalPlan(plan.getTemporalPlanActions());
            } else {
                throw new IllegalStateException("Cannot save plan of type: " + plan.getClass());
            }
        }
    }

    /**
     * Reset the planner state.
     */
    protected void resetState() {
        random = null;
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    /**
     * Initialize the planner using the given problem. Precalculates the shortest paths using Floyd-Warshall.
     *
     * @param problem the problem
     */
    protected void initialize(Problem problem) {
        shortestPathMatrix = PlannerUtils.computeAPSP(problem.getRoadGraph());
        random = new Random(2017L);
    }

    /**
     * Find the nearest vehicle on the graph from the given location that has at least some capacity.
     *
     * @param vehicles the vehicles to look through
     * @param curLocation the current location
     * @param minFreeCapacity the minimum capacity the vehicle has to have
     * @return maybe the vehicle, or an empty optional if no such vehicle was found
     */
    protected Optional<Vehicle> nearestVehicle(Collection<Vehicle> vehicles, Location curLocation,
            int minFreeCapacity) {
        return Stream.ofAll(vehicles).filter(v -> v.getCurCapacity().getCost() >= minFreeCapacity)
                .minBy(v -> getShortestPathMatrix().get(v.getLocation().getName(), curLocation.getName()).getDistance())
                .toJavaOptional();
    }

    /**
     * Constructs a partial plan that delivers the chosen package using the chosen vehicle,
     * optionally picking up and dropping other vehicles on the way.
     *
     * @param domain the domain
     * @param current the current state
     * @param chosenVehicleName the vehicle we are generating actions for
     * @param chosenPackage the package we originally chose to deliver
     * @param unfinished a list of unfinished packages
     * @param weighPackagesByDistance should we deliver packages based on their proximity to their targets,
     * or just greedily those that are on the path?
     * @return a list of actions applicable to the state
     */
    protected List<Action> findPartialPlan(Domain domain, ImmutablePlanState current, String chosenVehicleName,
            final Package chosenPackage, List<Package> unfinished, boolean weighPackagesByDistance) {
        Map<Location, Set<Package>> packageLocMap = new HashMap<>();
        for (Package pkg : unfinished) {
            packageLocMap.computeIfAbsent(pkg.getLocation(), l -> new HashSet<>()).add(pkg);
        }

        final Vehicle chosenVehicle = current.getProblem().getVehicle(chosenVehicleName);
        Location packageLoc = chosenPackage.getLocation();
        List<RoadEdge> basicPath = new ArrayList<>();
        basicPath.addAll(getShortestPathMatrix().get(chosenVehicle.getLocation().getName(),
                packageLoc.getName()).getRoads());
        basicPath.addAll(getShortestPathMatrix().get(packageLoc.getName(),
                chosenPackage.getTarget().getName()).getRoads());
        if (basicPath.isEmpty()) {
            return Collections.emptyList();
        }
        List<Location> locationsOnPath = basicPath.stream().map(RoadEdge::getFrom).collect(Collectors.toList());
        locationsOnPath.add(basicPath.get(basicPath.size() - 1).getTo());

        Set<Package> packagesOnPath = new HashSet<>();
        for (RoadEdge edge : basicPath) {
            Set<Package> atFrom = packageLocMap.get(edge.getFrom());
            if (atFrom != null) {
                packagesOnPath.addAll(atFrom);
            }
        }

        javaslang.collection.List<Tuple3<Integer, Integer, Package>> completelyOnPath = Stream.ofAll(packagesOnPath)
                .map(p -> Tuple.of(locationsOnPath.indexOf(p.getLocation()),
                        locationsOnPath.lastIndexOf(p.getTarget()), p))
                .filter(t -> t._1 >= 0 && t._2 >= 0 && t._1 <= t._2).toList();
        // only packages with pick up and drop on path and with pick up before drop
        // list should have at least one entry now
        final int curCapacity = chosenVehicle.getCurCapacity().getCost();
        List<Package> chosenPackages;
        Map<Package, Location> targetMap;
        if (weighPackagesByDistance) {
            int capacityLeft;
            if ((long) completelyOnPath.map(t -> t._3.getSize().getCost()).sum() > curCapacity) {
                // if not take everything
                Tuple3<Integer, Integer, javaslang.collection.List<Package>> pkgTuples = Stream.ofAll(completelyOnPath)
                        .combinations().map(Value::toList)
                        .map(sTuple -> Tuple.of(curCapacity - PlannerUtils.calculateMaxCapacity(sTuple),
                                sTuple.map(t -> t._3)))
                        .filter(t -> t._1 >= 0).map(t -> Tuple.of(t._1, t._2.toStream().map(p -> locationsOnPath
                                .lastIndexOf(p.getTarget())).max().getOrElse(Integer.MAX_VALUE), t._2))
                        .minBy(t -> Tuple.of(t._1, t._2)).get();
                // TODO: T2 not last index, but length of indexes + maximize?

                capacityLeft = pkgTuples._1;
                chosenPackages = pkgTuples._3.toJavaList();
            } else {
                Tuple3<Integer, Integer, javaslang.collection.List<Package>> pkgTuples
                        = Tuple.of(curCapacity - PlannerUtils.calculateMaxCapacity(completelyOnPath),
                        completelyOnPath.toStream().map(t -> locationsOnPath
                                .lastIndexOf(t._3.getTarget())).max().getOrElse(Integer.MAX_VALUE),
                        completelyOnPath.map(t -> t._3).toList());
                capacityLeft = pkgTuples._1;
                chosenPackages = pkgTuples._3.toJavaList();
            }
            targetMap = calculateTargetMap(capacityLeft, chosenPackages, packagesOnPath, basicPath);
        } else {
            if ((long) completelyOnPath.map(t -> t._3.getSize().getCost()).sum() > curCapacity) {
                // if not take everything
                chosenPackages = Stream.ofAll(completelyOnPath).combinations().map(Value::toList)
                        .map(sTuple -> Tuple.of(curCapacity - PlannerUtils.calculateMaxCapacity(sTuple),
                                sTuple.map(t -> t._3)))
                        .filter(t -> t._1 >= 0).map(t -> Tuple.of(t._1, t._2.toStream().map(p -> locationsOnPath
                                .lastIndexOf(p.getTarget())).max().getOrElse(Integer.MAX_VALUE), t._2))
                        .minBy(t -> Tuple.of(t._1, t._2)) // TODO: T2 not last index, but length of indexes + maximize?
                        .map(t -> t._3.toJavaList()).getOrElse((List<Package>) null);
            } else {
                chosenPackages = completelyOnPath.map(t -> t._3).toJavaList();
            }
            targetMap = Collections.emptyMap();
        }

        if (chosenPackages == null) {
            throw new IllegalStateException("Should not occur.");
        }
        return PlannerUtils.buildPlan(domain, basicPath, chosenVehicle, chosenPackages, targetMap);
    }

    /**
     * Calculate the target map, used for packages that get moved closer, not necessarily directly to their target.
     *
     * @param capacityLeft the capacity left in the vehicle we want to fill
     * @param chosenPackages the chosen packages so far (will add packages to this list)
     * @param packagesOnPath all packages that are currently dropped at a location on the path
     * @param path the path
     * @return a map of packages and their destinations on the path
     */
    private Map<Package, Location> calculateTargetMap(int capacityLeft, List<Package> chosenPackages,
            Set<Package> packagesOnPath, List<RoadEdge> path) {
        Map<Package, Location> targetMap = new HashMap<>(chosenPackages.size() + capacityLeft);
        for (Package pkg : chosenPackages) {
            targetMap.put(pkg, pkg.getTarget());
        }

        if (capacityLeft > 0) { // fill in with packages that get closer to goal
            List<Tuple3<Integer, Location, Package>> distancesFromPath = new ArrayList<>(capacityLeft * 1);
            for (Package pkg : packagesOnPath) {
                if (chosenPackages.contains(pkg)) {
                    continue;
                }
                List<Tuple2<Integer, Location>> distancesFromPathLocation = new ArrayList<>();
                boolean pickedUp = false;
                boolean first = true;
                for (RoadEdge edge : path) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (edge.getFrom().equals(pkg.getLocation())) {
                        pickedUp = true;
                        continue;
                    }
                    if (pickedUp) {
                        ShortestPath pkgToTarget = getShortestPathMatrix().get(edge.getFrom().getName(),
                                pkg.getTarget().getName());
                        distancesFromPathLocation.add(Tuple.of(pkgToTarget.getDistance(), edge.getFrom()));
                    }
                }
                if (!distancesFromPathLocation.isEmpty()) {
                    Tuple2<Integer, Location> minLoc = Collections.min(distancesFromPathLocation,
                            Comparator.comparing(t -> t._1));
                    distancesFromPath.add(Tuple.of(minLoc._1, minLoc._2, pkg));
                }
            }
            distancesFromPath.sort(Comparator.comparing(t -> t._1));
            for (Tuple3<Integer, Location, Package> tuple : distancesFromPath) {
                int pkgSize = tuple._3.getSize().getCost();
                if (capacityLeft - pkgSize < 0) {
                    continue;
                }
                capacityLeft -= pkgSize;

                chosenPackages.add(tuple._3);
                targetMap.put(tuple._3, tuple._2);
            }
        }
        return targetMap;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
}
