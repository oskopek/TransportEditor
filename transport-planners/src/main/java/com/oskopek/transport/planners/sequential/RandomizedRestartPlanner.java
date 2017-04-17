package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
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
import javaslang.collection.Stream;
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

import java.util.*;
import java.util.stream.Collectors;

public class RandomizedRestartPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ArrayTable<String, String, ShortestPath> shortestPathMatrix;
    private Random random;
    private Plan bestPlan;
    private int bestPlanScore;

    public RandomizedRestartPlanner() {
        setName(RandomizedRestartPlanner.class.getSimpleName());
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
        shortestPathMatrix = RandomizedRestartPlanner.computeAPSP(problem.getRoadGraph());
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
                Set<Package> unfinished = PlannerUtils.getUnfinishedPackages(curProblem.getAllPackages());
                if (unfinished.isEmpty()) {
                    throw new IllegalStateException("Zero packages left but not in goal state.");
                }

                Vehicle chosenVehicle = vehicles.get(random.nextInt(vehicles.size()));
                List<Package> chosenPackages = new ArrayList<>();
                Location chosenLocation;
                while (true) {
                    chosenLocation = locations.get(random.nextInt(locations.size()));
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

                List<Action> newActions = findPlan(domain, current, chosenVehicle.getName(), chosenPackages);
                current = javaslang.collection.Stream.ofAll(newActions).foldLeft(Optional.of(current),
                        (state, action) -> state.flatMap(state2 -> state2.apply(action)))
                        .orElseThrow(() -> new IllegalStateException("Could not apply all new actions to current state."));

                // TODO: assert that packages were delivered
                if (shouldCancel()) {
                    logger.debug("Cancelling, returning best found plan so far with score: {}.", bestPlanScore);
                    return Optional.ofNullable(bestPlan);
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Finished one iteration. Length: {}", current.getTotalTime());
            }

            if (bestPlanScore > current.getTotalTime()) {
                logger.debug("Found new best plan {} -> {}", bestPlanScore, current.getTotalTime());
                bestPlanScore = current.getTotalTime();
                bestPlan = new SequentialPlan(current.getAllActionsInList()); // TODO: collapse?
            }
        }
    }

    private List<Action> findPlan(Domain domain, ImmutablePlanState current, String chosenVehicleName,
            List<Package> chosenPackages) {
        List<Action> actions = new ArrayList<>();
        if (chosenPackages.isEmpty()) {
            return actions;
        }

        final Vehicle chosenVehicle = current.getProblem().getVehicle(chosenVehicleName);
        Location packageLoc = chosenPackages.get(0).getLocation();

        // drive to packages
        ShortestPath toPackages = shortestPathMatrix.get(chosenVehicle.getLocation().getName(), chosenPackages.get(0).getLocation().getName());
        toPackages.getRoads().forEach(re -> actions.add(domain.buildDrive(chosenVehicle, re.getFrom(), re.getTo(), re.getRoad())));

        // pick packages up
        chosenPackages.forEach(p -> actions.add(domain.buildPickUp(chosenVehicle, p.getLocation(), p)));

        // drive to each target
        Stream.ofAll(chosenPackages).map(pkg -> pkg.getTarget().getName()).distinct().permutations()
                .map(Stream::toList).map(lList -> lList.prepend(packageLoc.getName())).map(lList -> lList.zip(lList.toStream().drop(1)))
                .map(lTuples -> lTuples.map(lTuple -> shortestPathMatrix.get(lTuple._1, lTuple._2)))
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
