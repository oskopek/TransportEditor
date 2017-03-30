package com.oskopek.transporteditor.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.state.ImmutablePlanState;
import com.oskopek.transport.model.state.ProblemPlanningWrapper;
import com.oskopek.transporteditor.planners.AbstractPlanner;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teneighty.heap.AbstractHeap;
import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap;

import java.util.*;
import java.util.stream.Stream;

public class ForwardAstarPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ProblemPlanningWrapper, Integer> fScore;
    private Map<ProblemPlanningWrapper, Integer> gScore;
    private Map<ProblemPlanningWrapper, Integer> hScore;
    private Map<ImmutablePlanState, Heap.Entry<Integer, ImmutablePlanState>> entryMap;
    private Set<ProblemPlanningWrapper> closedSet;
    private AbstractHeap<Integer, ImmutablePlanState> openSet;
    private ObjectProperty<ArrayTable<String, String, Integer>> distanceMatrix = new SimpleObjectProperty<>();
    private Plan bestPlan;
    private int bestPlanScore;

    public ForwardAstarPlanner() {
        setName(ForwardAstarPlanner.class.getSimpleName());
    }

    private Integer getHScore(ImmutablePlanState state) {
        return hScore.computeIfAbsent(state, s -> calculateHeuristic(s, getDistanceMatrix(),
                PlannerUtils.getUnfinishedPackages(s.getAllPackages())));
    }

    private Integer getFScore(ImmutablePlanState state) {
        return fScore.getOrDefault(state, Integer.MAX_VALUE);
    }

    private Integer getGScore(ImmutablePlanState state) {
        return gScore.getOrDefault(state, Integer.MAX_VALUE);
    }

    public ArrayTable<String, String, Integer> getDistanceMatrix() {
        return distanceMatrix.get();
    }

    void resetState() {
        hScore = new HashMap<>();
        fScore = new HashMap<>();
        closedSet = new HashSet<>();
        openSet = new BinaryHeap<>();
        entryMap = new HashMap<>();
        gScore = new HashMap<>();
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    void initialize(Domain domain, Problem problem) {
        this.distanceMatrix.setValue(PlannerUtils.computeAPSP(problem.getRoadGraph()));
        ImmutablePlanState start = new ImmutablePlanState(domain, problem, Collections.emptyList());
        int startHScore = getHScore(start);
        fScore.put(start, startHScore);
        entryMap.put(start, openSet.insert(startHScore, start));
        gScore.put(start, 0);
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(domain, problem);
        logger.debug("Starting planning...");

        while (!entryMap.isEmpty()) {
            ImmutablePlanState current = openSet.extractMinimum().getValue();
            entryMap.remove(current);
//            System.out.println("\n\n" + new SequentialPlanIO(domain, problem).serialize(new SequentialPlan(current
// .getActions().toJavaList())));
//            logger.debug("F: {}, G: {}, H: {}", getFScore(current), getGScore(current), getHScore(current));
            if (current.isGoalState()) {
//                logger.debug("Found goal state! Explored {} states. Left out {} states.", closedSet.size(),
//                        openSet.getKeys().size());
                if (bestPlanScore > current.getTotalTime()) {
                    logger.debug("Found new best plan {} -> {}", bestPlanScore, current.getTotalTime());
                    bestPlanScore = current.getTotalTime();
                    bestPlan = new SequentialPlan(current.getActions());
                }
                return Optional.of(new SequentialPlan(current.getActions())); // TODO: remove me?
            }

            if (shouldCancel()) {
                logger.debug("Cancelling, returning best found plan so with score: {}.", bestPlanScore);
                return Optional.ofNullable(bestPlan);
            }

            closedSet.add(new ProblemPlanningWrapper(current));

            Stream<Action> generatedActions = PlannerUtils.generateActions(current, current.getActions(),
                    distanceMatrix.get(), PlannerUtils.getUnfinishedPackages(current.getAllPackages()));
            generatedActions.forEach(generatedAction -> {
                // Ignore the neighbor state which is already evaluated or invalid
                Optional<ImmutablePlanState> maybeNeighbor = current.apply(generatedAction)
                        .filter(state -> !closedSet.contains(state));
                if (maybeNeighbor.isPresent()) {
                    ImmutablePlanState neighbor = maybeNeighbor.get();

                    // The distance from start to a neighbor
                    int tentativeGScore = getGScore(current) + generatedAction.getDuration().getCost();
                    int neighborFScore = tentativeGScore + getHScore(neighbor);
                    int neighborGScore = getGScore(neighbor);

                    Heap.Entry<Integer, ImmutablePlanState> neighborEntry = entryMap.get(neighbor);
                    if (neighborEntry == null) {
                        neighborEntry = openSet.insert(neighborFScore, neighbor);
                        entryMap.put(neighbor, neighborEntry);
                    } else if (tentativeGScore >= neighborGScore) {
//                        if (tentativeGScore > neighborGScore) {
//                             TODO: P22 nonopt, p03 nonopt, p04
//                            logger.debug("Try not to generate these plans");
//                        }
                        return;
                    }

                    // this path is the best until now
                    openSet.decreaseKey(neighborEntry,
                            neighborFScore); // TODO check if overwrites the correct state with shorter actions
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, neighborFScore);
                }
            });
            if (closedSet.size() % 100_000 == 0) {
                logger.debug("Explored {} states, left: {} ({})", closedSet.size(), openSet.getEntries().size(),
                        entryMap.size());
                logger.debug("Current plan depth: {}", current.getActions().size());
            }
        }

        return Optional.ofNullable(bestPlan);
    }

    private static Integer calculateHeuristic(ProblemPlanningWrapper state,
            ArrayTable<String, String, Integer> distanceMatrix, Collection<Package> unfinishedPackages) {
        return PlannerUtils.calculateSumOfDistancesToPackageTargets(unfinishedPackages, state.getAllVehicles(),
                distanceMatrix);
    }

    @Override
    public ForwardAstarPlanner copy() {
        return new ForwardAstarPlanner();
    }

}
