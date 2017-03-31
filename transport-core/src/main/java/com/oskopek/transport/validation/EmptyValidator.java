package com.oskopek.transport.validation;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.tools.executables.AbstractLogStreamable;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof EmptyValidator;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
