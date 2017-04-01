package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.AbstractPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teneighty.heap.AbstractHeap;
import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap;

import java.util.*;
import java.util.stream.Stream;

public class ForwardAstarPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ImmutablePlanState, Heap.Entry<Integer, ImmutablePlanState>> entryMap;
    private Set<ImmutablePlanState> closedSet;
    private AbstractHeap<Integer, ImmutablePlanState> openSet;
    private ArrayTable<String, String, Integer> distanceMatrix;
    private Plan bestPlan;
    private int bestPlanScore;

    public ForwardAstarPlanner() {
        setName(ForwardAstarPlanner.class.getSimpleName());
    }

    private Integer getHScore(ImmutablePlanState s) {
        return calculateHeuristic(s, distanceMatrix,
                PlannerUtils.getUnfinishedPackages(s.getProblem().getAllPackages()));
    }

    public ArrayTable<String, String, Integer> getDistanceMatrix() {
        return distanceMatrix;
    }

    void resetState() {
        closedSet = new HashSet<>();
        openSet = new BinaryHeap<>();
        entryMap = new HashMap<>();
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    void initialize(Problem problem) {
        distanceMatrix = PlannerUtils.computeAPSP(problem.getRoadGraph());
        ImmutablePlanState start = new ImmutablePlanState(problem);
        int startHScore = getHScore(start);
        entryMap.put(start, openSet.insert(startHScore, start));
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        logger.debug("Initializing planning...");
        resetState();
        initialize(problem);
        logger.debug("Starting planning...");

        while (!entryMap.isEmpty()) {
            ImmutablePlanState current = openSet.extractMinimum().getValue();
            entryMap.remove(current);
            if (current.isGoalState()) {
                if (bestPlanScore > current.getTotalTime()) {
                    logger.debug("Found new best plan {} -> {}", bestPlanScore, current.getTotalTime());
                    bestPlanScore = current.getTotalTime();
                    bestPlan = new SequentialPlan(current.getAllActionsInList());
                }
                return Optional.of(new SequentialPlan(current.getAllActionsInList())); // TODO: remove me?
            }

            if (shouldCancel()) {
                logger.debug("Cancelling, returning best found plan so far with score: {}.", bestPlanScore);
                return Optional.ofNullable(bestPlan);
            }

            closedSet.add(current);

            Stream<Action> generatedActions = PlannerUtils.generateActions(domain, current, distanceMatrix,
                    PlannerUtils.getUnfinishedPackages(current.getProblem().getAllPackages()));
            generatedActions.forEach(generatedAction -> {
                // Ignore the neighbor state which is already evaluated or invalid
                Optional<ImmutablePlanState> maybeNeighbor = current.apply(generatedAction)
                        .filter(state -> !closedSet.contains(state));
                if (maybeNeighbor.isPresent()) {
                    ImmutablePlanState neighbor = maybeNeighbor.get();

                    // The distance from start to a neighbor
                    int tentativeGScore = neighbor.getTotalTime(); // G score
                    int neighborFScore = tentativeGScore + getHScore(neighbor);

                    Heap.Entry<Integer, ImmutablePlanState> neighborEntry = entryMap.get(neighbor);
                    if (neighborEntry == null) {
                        neighborEntry = openSet.insert(neighborFScore, neighbor);
                        entryMap.put(neighbor, neighborEntry);
                    } else if (tentativeGScore >= neighborEntry.getValue().getTotalTime()) {
//                        if (tentativeGScore > neighborGScore) {
//                             TODO: P22 nonopt, p03 nonopt, p04
//                            logger.debug("Try not to generate these plans");
//                        }
                        return;
                    }

                    // this path is the best until now
                    openSet.decreaseKey(neighborEntry,
                            neighborFScore); // TODO check if overwrites the correct state with shorter actions
                }
            });
            if (closedSet.size() % 100_000 == 0) {
                logger.debug("Explored {} states, left: {} ({})", closedSet.size(), openSet.getEntries().size(),
                        entryMap.size());
            }
        }

        return Optional.ofNullable(bestPlan);
    }

    private static Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, Integer> distanceMatrix, Collection<Package> unfinishedPackages) {
        return PlannerUtils.calculateSumOfDistancesToPackageTargets(unfinishedPackages,
                state.getProblem().getAllVehicles(), distanceMatrix);
    }

    @Override
    public ForwardAstarPlanner copy() {
        return new ForwardAstarPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForwardAstarPlanner;
    }
}
