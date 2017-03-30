package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.AbstractPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * Breadth-first forward search planner. Still has bugs, highly experimental and very slow.
 *
 * @deprecated Probably not correct.
 */
public class ForwardBFSPlanner extends AbstractPlanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor.
     */
    public ForwardBFSPlanner() {
        setName(ForwardAstarPlanner.class.getSimpleName());
    }

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        ArrayTable<String, String, Integer> distanceMatrix = PlannerUtils.computeAPSP(problem.getRoadGraph());

        Deque<ImmutablePlanState> states = new ArrayDeque<>();
        states.add(new ImmutablePlanState(domain, problem, Collections.emptyList()));

        logger.debug("Starting planning...");

        long counter = 0;
        List<Action> actions = Collections.emptyList();
        while (!states.isEmpty()) {
            if (shouldCancel()) {
                logger.debug("Returning current hypothesis plan after cancellation.");
                return Optional.of(new SequentialPlan(actions));
            }
            ImmutablePlanState state = states.removeFirst();
            if (state.getActions().size() > actions.size()) {
                logger.debug("Enlarged plan: {} actions", state.getActions().size());
                logger.debug("Explored {} states, left: {}", counter, states.size());
            }
            actions = state.getActions();

            if (state.isGoalState()) {
                logger.debug("Found goal state! Exiting. Explored {} states. Left out {} states.", counter,
                        states.size());
                return Optional.of(new SequentialPlan(actions));
            }

            Stream.Builder<ImmutablePlanState> generatedStates = Stream.builder();
            Stream<Action> generatedActions = PlannerUtils.generateActions(state, state.getActions(),
                    distanceMatrix, PlannerUtils.getUnfinishedPackages(state.getAllPackages()));
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
                logger.debug("Explored {} states, left: {}", counter, states.size());
            }
        }

        return Optional.empty();
    }

    @Override
    public ForwardBFSPlanner copy() {
        return new ForwardBFSPlanner();
    }
}
