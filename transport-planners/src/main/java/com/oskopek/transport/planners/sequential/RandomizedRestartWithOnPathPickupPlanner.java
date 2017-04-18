package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Value;
import javaslang.collection.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.dnd.DragGestureEvent;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// choose a random vehicle and package,
// find the path, find all packages on it
// choose the ones whose target is on the path
// if still not fully capacitated, choose the others in the
// order of distance from the shortest path
public class RandomizedRestartWithOnPathPickupPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ArrayTable<String, String, ShortestPath> shortestPathMatrix;
    private Random random;
    private Plan bestPlan;
    private int bestPlanScore;

    public RandomizedRestartWithOnPathPickupPlanner() {
        setName(RandomizedRestartWithOnPathPickupPlanner.class.getSimpleName());
    }

    public ArrayTable<String, String, ShortestPath> getShortestPathMatrix() {
        return shortestPathMatrix;
    }

    void resetState() {
        random = null;
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    void initialize(Problem problem) {
        shortestPathMatrix = RandomizedRestartWithOnPathPickupPlanner.computeAPSP(problem.getRoadGraph());
        random = new Random(2017L);
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
            while (!current.isGoalState() && current.getTotalTime() < bestPlanScore) {
                Problem curProblem = current.getProblem();
                List<Package> unfinished = new ArrayList<>(PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages()));
                if (unfinished.isEmpty()) {
                    throw new IllegalStateException("Zero packages left but not in goal state.");
                }

                Vehicle chosenVehicle = vehicles.get(random.nextInt(vehicles.size()));
                Package chosenPackage;
                while (true) {
                    chosenPackage = unfinished.get(random.nextInt(unfinished.size()));
                    int curCapacity = chosenVehicle.getCurCapacity().getCost();
                    curCapacity -= chosenPackage.getSize().getCost();
                    if (curCapacity >= 0) {
                        break;
                    }
                }

                List<Action> newActions = findPartialPlan(domain, current, chosenVehicle.getName(), chosenPackage, unfinished);
                current = Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to current state."));

                if (shouldCancel()) {
                    logger.debug("Cancelling, returning best found plan so far with score: {}.", bestPlanScore);
                    return Optional.ofNullable(bestPlan);
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Finished one iteration. Length: {}", current.getTotalTime());
            }

            // TODO: collapse plan?
            if (bestPlanScore > current.getTotalTime()) {
                logger.debug("Found new best plan {} -> {}", bestPlanScore, current.getTotalTime());
                bestPlanScore = current.getTotalTime();
                bestPlan = new SequentialPlan(current.getAllActionsInList());
            }
        }
    }

    private List<Action> findPartialPlan(Domain domain, ImmutablePlanState current, String chosenVehicleName,
            final Package chosenPackage, List<Package> unfinished) {
        Map<Location, Set<Package>> packageLocMap = new HashMap<>();
        for (Package pkg : unfinished) {
            packageLocMap.computeIfAbsent(pkg.getLocation(), l -> new HashSet<>()).add(pkg);
        }

        final Vehicle chosenVehicle = current.getProblem().getVehicle(chosenVehicleName);
        Location packageLoc = chosenPackage.getLocation();
        List<RoadEdge> basicPath = new ArrayList<>();
        basicPath.addAll(shortestPathMatrix.get(chosenVehicle.getLocation().getName(), packageLoc.getName()).getRoads());
        basicPath.addAll(shortestPathMatrix.get(packageLoc.getName(), chosenPackage.getTarget().getName()).getRoads());
        if (basicPath.isEmpty()) {
            return Collections.emptyList();
        }
        List<Location> locationsOnPath = basicPath.stream().map(RoadEdge::getFrom).collect(Collectors.toList());
        locationsOnPath.add(basicPath.get(basicPath.size() - 1).getTo());

        List<Package> packagesOnPath = new ArrayList<>();
        for (RoadEdge edge : basicPath) {
            Set<Package> atFrom = packageLocMap.get(edge.getFrom());
            if (atFrom != null) {
                packagesOnPath.addAll(atFrom);
            }
        }

        javaslang.collection.List<Tuple3<Integer, Integer, Package>> completelyOnPath = Stream.ofAll(packagesOnPath)
                .map(p -> Tuple.of(locationsOnPath.indexOf(p.getLocation()), locationsOnPath.lastIndexOf(p.getTarget()), p))
                .filter(t -> t._1 >= 0 && t._2 >= 0 && t._1 <= t._2).toList();
        // only packages with pick up and drop on path and with pick up before drop
        // list should have at least one entry now
        // TODO: try taking packages which get closer to target too
        int curCapacity = chosenVehicle.getCurCapacity().getCost();
        List<Package> chosenPackages = Stream.ofAll(completelyOnPath).combinations().map(Value::toList)
                .map(sTuple -> Tuple.of(curCapacity - calculateMaxCapacity(sTuple), sTuple.map(t -> t._3)))
                .filter(t -> t._1 >= 0).minBy(t -> t._1).map(t -> t._2.toJavaList()).getOrElse((List<Package>) null);
        if (chosenPackages == null) {
            throw new IllegalStateException("Should not occur.");
        }

        // TODO: try creating a bit different path
        List<RoadEdge> path = basicPath;
        Map<Location, Set<Package>> packagesPickupDrop = new HashMap<>(chosenPackages.size() * 2);
        for (Location location : locationsOnPath) { // packages have to guarantee not being dropped before pick up now
            chosenPackages.stream().filter(p -> location.equals(p.getLocation()) || location.equals(p.getTarget()))
                    .forEach(p -> packagesPickupDrop.computeIfAbsent(location, l -> new HashSet<>()).add(p));
        }
        return buildPlan(domain, path, chosenVehicle, packagesPickupDrop);
    }

    private static Integer calculateMaxCapacity(javaslang.collection.List<Tuple3<Integer, Integer, Package>> combination) {
        return combination.flatMap(t -> Stream.of(Tuple.of(t._1, false), Tuple.of(t._2, true)))
                .sortBy(t -> t._1).foldLeft(Tuple.of(0, Integer.MIN_VALUE), (capTuple, elem) -> {
                    int curCapacity = capTuple._1;
                    if (elem._2) { // drop
                        curCapacity -= 1; // TODO assumes 1 sizes of packages
                    } else { // pickup
                        curCapacity += 1; // TODO assumes 1 sizes of packages
                    }
                    if (curCapacity > capTuple._2) {
                        return Tuple.of(curCapacity, curCapacity);
                    } else {
                        return Tuple.of(curCapacity, capTuple._2);
                    }
                })._2;
    }

    private List<Action> buildPlan(Domain domain, List<RoadEdge> path, Vehicle vehicle,
            Map<Location, Set<Package>> packagesPickupDrop) {
        if (path.isEmpty()) {
            return Collections.emptyList();
        }
        List<Action> actions = new ArrayList<>();
        Set<Package> inVehicle = new HashSet<>(vehicle.getPackageList());
        for (int i = path.size() - 1; i >= 0; i--) {
            RoadEdge edge = path.get(i);
            Location to = edge.getTo();
            buildPackageActions(domain, actions, inVehicle, packagesPickupDrop.get(to), to, vehicle);
            packagesPickupDrop.remove(to);

            // drive
            actions.add(domain.buildDrive(vehicle, edge.getFrom(), to, edge.getRoad()));
        }
        // last loc
        Location firstLocation = path.get(0).getFrom();
        buildPackageActions(domain, actions, inVehicle, packagesPickupDrop.get(firstLocation), firstLocation, vehicle);

        for (int i = 0; i < actions.size(); i++) { // remove redundant drives
            Action action = actions.get(i);
            if (action instanceof Drive) {
                actions.remove(i);
            } else {
                break;
            }
        }
        return Stream.ofAll(actions).reverse().toJavaList();
    }

    private void buildPackageActions(Domain domain, List<Action> actions, Set<Package> inVehicle, Set<Package> atFrom, Location from, Vehicle vehicle) {
        if (atFrom != null) {
            for (Package pkg : atFrom) {
                if (inVehicle.contains(pkg)) {
                    actions.add(domain.buildPickUp(vehicle, from, pkg));
                    inVehicle.remove(pkg);
                } else {
                    actions.add(domain.buildDrop(vehicle, from, pkg));
                    inVehicle.add(pkg);
                }
            }
        }
    }


    @Override
    public RandomizedRestartWithOnPathPickupPlanner copy() {
        return new RandomizedRestartWithOnPathPickupPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RandomizedRestartWithOnPathPickupPlanner;
    }

    private static ArrayTable<String, String, ShortestPath> computeAPSP(final RoadGraph graph) {
        final String ATTRIBUTE_NAME = "weight";
        RoadGraph originalAPSPGraph = (RoadGraph) Graphs.clone(graph);
        originalAPSPGraph.getAllRoads().forEach(roadEdge -> originalAPSPGraph.getEdge(roadEdge.getRoad().getName())
                .addAttribute(ATTRIBUTE_NAME, roadEdge.getRoad().getLength().getCost()));
        new APSP(originalAPSPGraph, ATTRIBUTE_NAME, true).compute();
        List<String> locationNames = originalAPSPGraph.getNodeSet().stream().map(Element::getId).collect(
                Collectors.toList());
        ArrayTable<String, String, ShortestPath> distanceMatrix = ArrayTable.create(locationNames, locationNames);
        for (String from : locationNames) {
            APSP.APSPInfo current = originalAPSPGraph.getNode(from).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (String to : locationNames) {
                Path shortestPath = current.getShortestPathTo(to);
                List<RoadEdge> roads = new ArrayList<>(shortestPath.getEdgeCount());
                int distance = (int) PlannerUtils.getLengthToCorrect(current, to);
                if (distance > 0) {
                    for (Edge edge : shortestPath.getEachEdge()) {
                        roads.add(graph.getRoadEdge(edge.getId()));
                    }
                }
                if (null != distanceMatrix.put(from, to, new ShortestPath(roads, distance))) {
                    throw new IllegalStateException("Overwritten a value.");
                }
            }
        }
        return distanceMatrix;
    }

    private static final class ShortestPath {

        private final List<RoadEdge> roads;
        private final Integer distance;

        public ShortestPath(List<RoadEdge> roads, Integer distance) {
            this.roads = roads;
            this.distance = distance;
        }

        public Location lastLocation() {
            if (roads.isEmpty()) {
                return null;
            }
            return roads.get(roads.size() - 1).getTo();
        }

        /**
         * Get the roads.
         *
         * @return the roads
         */
        public List<RoadEdge> getRoads() {
            return roads;
        }

        /**
         * Get the distance.
         *
         * @return the distance
         */
        public Integer getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("distance", distance).append("roads", roads).toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof ShortestPath)) {
                return false;
            }

            ShortestPath that = (ShortestPath) o;

            return new EqualsBuilder().append(getRoads(), that.getRoads()).append(getDistance(), that.getDistance())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getRoads()).append(getDistance()).toHashCode();
        }
    }
}
