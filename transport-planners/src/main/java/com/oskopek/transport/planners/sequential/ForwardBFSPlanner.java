package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.AbstractPlanner;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * Breadth-first forward search planner. Still has bugs, highly experimental and very slow.
 *
 * @deprecated Probably not correct.
 */
public class ForwardBFSPlanner extends AbstractPlanner {

    /**
     * Default constructor.
     */
    public ForwardBFSPlanner() {
        setName(ForwardBFSPlanner.class.getSimpleName());
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        ArrayTable<String, String, ShortestPath> distanceMatrix = PlannerUtils.computeAPSP(problem.getRoadGraph());

        Deque<ImmutablePlanState> states = new ArrayDeque<>();
        states.add(new ImmutablePlanState(problem));

        formatLog("Starting planning...");

        long counter = 0;
        while (!states.isEmpty()) {
            ImmutablePlanState state = states.removeFirst();
            if (shouldCancel()) {
                formatLog("Returning current hypothesis plan after cancellation.");
                return Optional.of(new SequentialPlan(state.getAllActionsInList()));
            }


            if (state.isGoalState()) {
                formatLog("Found goal state! Exiting. Explored {} states. Left out {} states.", counter,
                        states.size());
                return Optional.of(new SequentialPlan(state.getAllActionsInList()));
            }

            Stream.Builder<ImmutablePlanState> generatedStates = Stream.builder();
            Stream<Action> generatedActions = PlannerUtils.generateActions(domain, state, distanceMatrix,
                    PlannerUtils.getUnfinishedPackages(state.getProblem().getAllPackages()));
            generatedActions.forEach(generatedAction -> {
                Optional<ImmutablePlanState> maybeNewState = state.apply(generatedAction);
                if (maybeNewState.isPresent()) {
                    ImmutablePlanState newState = maybeNewState.get();
                    generatedStates.accept(newState);
                }
            });

            generatedStates.build().sorted(Comparator.comparing(ImmutablePlanState::getTotalTime))
                    .forEach(states::addLast);

            counter++;
            if (counter % 100_000 == 0) {
                formatLog("Explored {} states, left: {}", counter, states.size());
            }
        }

        return Optional.empty();
    }

    @Override
    public ForwardBFSPlanner copy() {
        return new ForwardBFSPlanner();
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForwardBFSPlanner;
    }
}
