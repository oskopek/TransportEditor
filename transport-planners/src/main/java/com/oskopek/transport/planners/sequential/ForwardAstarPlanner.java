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
import javaslang.Tuple;
import javaslang.Tuple2;
import org.slf4j.LoggerFactory;
import org.teneighty.heap.AbstractHeap;
import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A forward planner using A* as search. Utilized {@link PlannerUtils}
 * for generating actions. Uses a binary heap ({@link org.teneighty.heap.BinaryHeap}) internally to keep track
 * of open states.
 */
public abstract class ForwardAstarPlanner extends AbstractPlanner {

    private Map<ImmutablePlanState, Heap.Entry<Integer, ImmutablePlanState>> entryMap;
    private Set<ImmutablePlanState> closedSet;
    private AbstractHeap<Integer, ImmutablePlanState> openSet;
    private ArrayTable<String, String, ShortestPath> distanceMatrix;
    private Plan myBestPlan;
    private int myBestPlanScore = Integer.MAX_VALUE;
    private boolean stopAtFirstSolution;

    /**
     * Default constructor.
     *
     * @param stopAtFirstSolution if the planner should stop after finding the first valid plan
     */
    public ForwardAstarPlanner(boolean stopAtFirstSolution) {
        setName(ForwardAstarPlanner.class.getSimpleName());
        this.stopAtFirstSolution = stopAtFirstSolution;
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Will stop at first solution?
     *
     * @return true iff will stop at first solution
     */
    protected boolean isStopAtFirstSolution() {
        return stopAtFirstSolution;
    }

    /**
     * Should stop at first solution?
     *
     * @param stopAtFirstSolution stop at first solution
     */
    protected void setStopAtFirstSolution(boolean stopAtFirstSolution) {
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
        resetBestPlan();
    }

    /**
     * Reset the best found plan to the original state.
     */
    void resetBestPlan() {
        myBestPlan = null;
        myBestPlanScore = Integer.MAX_VALUE;
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
    public Optional<Plan> plan(Domain domain, Problem problem, Function<Plan, Plan> planTransformation) {
        Optional<Plan> maybePlan = planInternal(domain, problem, planTransformation);
        closedSet = null;
        openSet = null;
        entryMap = null;
        distanceMatrix = null;
        return maybePlan;
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        return plan(domain, problem, null);
    }

    private ImmutablePlanState idaStar(Domain domain, ImmutablePlanState start) {
        int bound = getHScore(start);
        while (true) {
            Tuple2<Integer, ImmutablePlanState> newBoundTuple = search(domain, start, 0, bound);
            int newBound = newBoundTuple._1;
            ImmutablePlanState resultState = newBoundTuple._2;
            if (resultState != null) {
                return resultState;
            }
            if (newBound < 0) {
                return null;
            }
            bound = newBound;
            if (shouldCancel()) {
                formatLog("Cancelling, returning best found plan so far with score: {}.", myBestPlanScore);
                return null;
            }
        }
    }

    private Tuple2<Integer, ImmutablePlanState> search(Domain domain, ImmutablePlanState node, int g, int bound) {
        int f = g + getHScore(node);
        if (f > bound) {
            return Tuple.of(f, null);
        }
        if (node.isGoalState()) {
            return Tuple.of(f, node);
        }
        int min = Integer.MAX_VALUE;
        List<Action> generatedActions = PlannerUtils.generateActions(domain, node, distanceMatrix,
                PlannerUtils.getUnfinishedPackages(node.getProblem().getAllPackages())).collect(Collectors.toList());
        for (Action action : generatedActions) {
            Optional<ImmutablePlanState> maybeNeighbor = node.apply(action)
                    .filter(state -> !closedSet.contains(state));
            if (maybeNeighbor.isPresent()) {
                ImmutablePlanState neighbor = maybeNeighbor.get();
                Tuple2<Integer, ImmutablePlanState> t = search(domain, neighbor, g + action.getCost().getCost(), bound);
                if (t._2 != null) {
                    return t;
                }
                if (t._1 < min) {
                    min = t._1;
                }
            }
        }
        return Tuple.of(min, null);
    }


    /**
     * Internal method for planning. Runs the A* algorithm.
     *
     * @param domain the domain
     * @param problem the problem
     * @param planTransformation the intermediate transformation function (used for temporal scheduling)
     * @return the plan, or an empty optional if no plan was found
     */
    private Optional<Plan> planInternal(Domain domain, Problem problem, Function<Plan, Plan> planTransformation) {
        formatLog("Initializing planning...");

        resetState();
        initialize(problem);
        formatLog("Starting planning...");
        ImmutablePlanState result = idaStar(domain, openSet.extractMinimum().getValue());
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new SequentialPlan(result.getAllActionsInList()));
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
