package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.Cancellable;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.LogStreamable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Represents a process that validates a plan against a given domain. See the {@link ValValidator} for an example.
 */
public interface Validator extends LogStreamable, Cancellable {

    /**
     * Runs the validation and reports the results.
     *
     * @param domain the domain to validate against
     * @param problem the problem to validate against
     * @param plan the plan to validate
     * @return true iff the plan is valid in the domain according to this validator
     */
    boolean isValid(Domain domain, Problem problem, Plan plan);

    @Override
    default boolean cancel() {
        // intentionally empty
        return false;
    }

    /**
     * Runs the validation and reports the results asynchronously.
     *
     * @param domain the domain to validate against
     * @param problem the problem to validate against
     * @param plan the plan to validate
     * @return true iff the plan is valid in the domain according to this validator
     */
    default CompletionStage<Boolean> isValidAsync(Domain domain, Problem problem, Plan plan) {
        return CompletableFuture.supplyAsync(() -> isValid(domain, problem, plan));
    }

    /**
     * Return the executable backing the validator. Implementations that do not have a backing executable
     * should return null.
     *
     * @return the backing executable or null if none such exists
     */
    default ExecutableWithParameters getExecutableWithParameters() {
        return null;
    }

}
