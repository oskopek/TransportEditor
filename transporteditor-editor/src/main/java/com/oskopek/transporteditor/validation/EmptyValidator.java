package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmptyValidator)) {
            return false;
        }
        EmptyValidator that = (EmptyValidator) o;
        return new EqualsBuilder().isEquals();
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
