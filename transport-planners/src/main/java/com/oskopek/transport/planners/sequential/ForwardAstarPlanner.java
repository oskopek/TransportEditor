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
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teneighty.heap.AbstractHeap;
import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap;

import java.util.*;
import java.util.stream.Stream;

/**
 * A forward planner using A* as search. Utilized {@link PlannerUtils}
 * for generating actions. Uses a binary heap ({@link org.teneighty.heap.BinaryHeap}) internally to keep track
 * of open states.
 */
public abstract class ForwardAstarPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ImmutablePlanState, Heap.Entry<Integer, ImmutablePlanState>> entryMap;
    private Set<ImmutablePlanState> closedSet;
    private AbstractHeap<Integer, ImmutablePlanState> openSet;
    private ArrayTable<String, String, ShortestPath> distanceMatrix;
    private Plan bestPlan;
    private int bestPlanScore;
    private final boolean stopAtFirstSolution;

    /**
     * Default constructor.
     *
     * @param stopAtFirstSolution if the planner should stop after finding the first valid plan
     */
    public ForwardAstarPlanner(boolean stopAtFirstSolution) {
        setName(ForwardAstarPlanner.class.getSimpleName());
        this.stopAtFirstSolution = stopAtFirstSolution;
    }

    /**
     * Get the heuristic score, used in A*.
     *
     * @param s the state whose score to get
     * @return the estimated distance to a goal state
     */
    private Integer getHScore(ImmutablePlanState s) {
        return calculateHeuristic(s, distanceMatrix,
                PlannerUtils.getUnfinishedPackages(s.getProblem().getAllPackages()));
    }

    /**
     * The distance matrix, used for checking if a path is the shortest possible.
     *
     * @return the shortest path length (sum of lengths of roads on the shortest paths)
     */
    public ArrayTable<String, String, ShortestPath> getDistanceMatrix() {
        return distanceMatrix;
    }

    /**
     * Resets to planner to an original state.
     */
    void resetState() {
        closedSet = new HashSet<>();
        openSet = new BinaryHeap<>();
        entryMap = new HashMap<>();
        bestPlan = null;
        bestPlanScore = Integer.MAX_VALUE;
    }

    /**
     * Initializes the planner, precomputing the shortest paths and initial scores.
     *
     * @param problem the problem to initialize the planner from
     */
    void initialize(Problem problem) {
        distanceMatrix = PlannerUtils.computeAPSP(problem.getRoadGraph());
        ImmutablePlanState start = new ImmutablePlanState(problem);
        int startHScore = getHScore(start);
        entryMap.put(start, openSet.insert(startHScore, start));
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        Optional<Plan> maybePlan = planInternal(domain, problem);
        closedSet = null;
        openSet = null;
        entryMap = null;
        distanceMatrix = null;
        return maybePlan;
    }

    /**
     * Internal method for planning. Runs the A* algorithm.
     *
     * @param domain the domain
     * @param problem the problem
     * @return the plan, or an empty optional if no plan was found
     */
    public Optional<Plan> planInternal(Domain domain, Problem problem) {
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
                if (stopAtFirstSolution) {
                    return Optional.of(new SequentialPlan(current.getAllActionsInList()));
                }
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
                        return;
                    }

                    // this path is the best until now
                    openSet.decreaseKey(neighborEntry, neighborFScore);
                }
            });
            if (closedSet.size() % 100_000 == 0) {
                logger.debug("Explored {} states, left: {} ({})", closedSet.size(), openSet.getEntries().size(),
                        entryMap.size());
            }
        }

        return Optional.ofNullable(bestPlan);
    }

    /**
     * Calculate the heuristic value for a given state (estimated distance to goal state).
     * Ideally, heuristics should be admissible (i.e. they should never overestimate the distance).
     * Then, A* returns the optimal solution first.
     *
     * @param state the state
     * @param distanceMatrix the shortest path length matrix
     * @param unfinishedPackages the packages that have not yet been delivered
     * @return the heuristic value (score)
     */
    protected abstract Integer calculateHeuristic(ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Collection<Package> unfinishedPackages);

    @Override
    public abstract ForwardAstarPlanner copy();

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForwardAstarPlanner;
    }
}
