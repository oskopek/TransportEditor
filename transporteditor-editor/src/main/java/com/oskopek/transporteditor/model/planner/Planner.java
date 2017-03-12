package com.oskopek.transporteditor.model.planner;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.Cancellable;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.LogStreamable;
import javafx.beans.value.ObservableValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Planners create {@link Plan}s from a given {@link Domain} and {@link Problem}.
 * An unenforced interface requirement: <strong>needs to have a no-arg public constructor.</strong>
 */
public interface Planner extends LogStreamable, Cancellable {

    /**
     * Starts planning asynchronously. Completes with the best found plan eventually, at the leisure of the planner
     * implementation.
     *
     * @param domain the domain to plan with
     * @param problem the problem to plan
     * @return a promise of a plan that is computed asynchronously
     */
    default CompletionStage<Plan> startAsync(Domain domain, Problem problem) {
        return CompletableFuture.supplyAsync(() -> startAndWait(domain, problem));
    }

    /**
     * Blocking planning call. Completes with the best found plan eventually, at the leisure of the planner
     * implementation.
     *
     * @param domain the domain to plan with
     * @param problem the problem to plan
     * @return the computed plan
     */
    Plan startAndWait(Domain domain, Problem problem);

    /**
     * Returns the currently best available plan to the planner. May return null until the
     * {@link #startAndWait(Domain, Problem)} completes (if it failed, may return null afterwards too).
     *
     * @return the current/best found plan (may be null)
     */
    default Plan getCurrentPlan() {
        return currentPlanProperty().getValue();
    }

    @Override
    default boolean cancel() {
        // intentionally empty
        return false;
    }

    /**
     * Observable value of the {@link #getCurrentPlan()}.
     *
     * @return the observable
     */
    ObservableValue<Plan> currentPlanProperty();

    /**
     * Observable boolean value indicating if planning is going on.
     *
     * @return a true observable iff the planner is currently planning, i.e. if its
     * {@link #startAndWait(Domain, Problem)} method was called and is blocking.
     */
    ObservableValue<Boolean> isPlanning();

    /**
     * Return the executable backing the planner. Implementations that do not have a backing executable
     * should return null.
     *
     * @return the backing executable or null if none such exists
     */
    default ExecutableWithParameters getExecutableWithParameters() {
        return null;
    }

    /**
     * Signifies if the given planner is available on this system.
     *
     * @return true iff this planner is available and ready to be executed on this system
     */
    boolean isAvailable();

    String getName();

}
