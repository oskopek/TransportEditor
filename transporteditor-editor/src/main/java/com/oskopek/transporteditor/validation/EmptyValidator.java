package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Dummy validator: assumes all plans are correct.
 */
public final class EmptyValidator extends AbstractLogStreamable implements Validator {

    @Override
    public boolean isValid(Domain domain, Problem problem, Plan plan) {
        return true;
    }

    @Override
    public CompletionStage<Boolean> isValidAsync(Domain domain, Problem problem, Plan plan) {
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }
}
