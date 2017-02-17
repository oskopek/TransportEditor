package com.oskopek.transporteditor.model;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.validation.EmptyValidator;
import com.oskopek.transporteditor.validation.Validator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * The default implementation of a planning session.
 */
public class DefaultPlanningSession implements PlanningSession {

    private final ObjectProperty<Planner> plannerProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Validator> validatorProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Domain> domainProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Problem> problemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Plan> planProperty = new SimpleObjectProperty<>();
    private final transient IntegerProperty sessionChange = new SimpleIntegerProperty();

    /**
     * Default constructor.
     */
    public DefaultPlanningSession() {
        setValidator(new EmptyValidator());
        plannerProperty().addListener(s -> registerChange());
        validatorProperty().addListener(s -> registerChange());
        domainProperty().addListener(s -> registerChange());
        problemProperty().addListener(s -> registerChange());
        planProperty().addListener(s -> registerChange());
    }

    /**
     * Register a change made to the session.
     */
    private void registerChange() {
        sessionChangeProperty().setValue(sessionChangeProperty().get() + 1);
    }

    /**
     * Session change property. Used for determining if a save as is needed.
     *
     * @return the session change property
     */
    private synchronized IntegerProperty sessionChangeProperty() {
        return sessionChange;
    }

    @Override
    public Domain getDomain() {
        return domainProperty.get();
    }

    @Override
    public void setDomain(Domain domain) {
        domainProperty.set(domain);
    }

    @Override
    public ObjectProperty<Domain> domainProperty() {
        return domainProperty;
    }

    @Override
    public Problem getProblem() {
        return problemProperty.get();
    }

    @Override
    public void setProblem(Problem problem) {
        problemProperty.set(problem);
    }

    @Override
    public ObjectProperty<Problem> problemProperty() {
        return problemProperty;
    }

    @Override
    public Plan getPlan() {
        return planProperty.get();
    }

    @Override
    public void setPlan(Plan plan) {
        planProperty.set(plan);
    }

    @Override
    public ObjectProperty<Plan> planProperty() {
        return planProperty;
    }

    @Override
    public Planner getPlanner() {
        return plannerProperty.get();
    }

    @Override
    public void setPlanner(Planner planner) {
        plannerProperty.set(planner);
    }

    @Override
    public ObjectProperty<Planner> plannerProperty() {
        return plannerProperty;
    }

    @Override
    public Validator getValidator() {
        return validatorProperty.get();
    }

    @Override
    public void setValidator(Validator validator) {
        validatorProperty.set(validator);
    }

    @Override
    public ObjectProperty<Validator> validatorProperty() {
        return validatorProperty;
    }

    @Override
    public CompletionStage<Plan> startPlanningAsync() {
        Planner planner = getPlanner();
        if (planner == null) {
            throw new IllegalStateException("Cannot plan with null planner.");
        }
        setPlan(null);
        return planner.startAsync(getDomain(), getProblem()).thenComposeAsync(plan -> {
            if (plan != null) {
                boolean isValid;
                try {
                    isValid = startValidationAsyncInternal(plan).toCompletableFuture().get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new IllegalStateException("Could not validate plan.", e);
                }
                if (isValid) {
                    setPlan(plan);
                } else {
                    throw new IllegalStateException("Resulting plan was invalid.");
                }
            }
            return CompletableFuture.completedFuture(plan);
        });
    }

    @Override
    public CompletionStage<Boolean> startValidationAsync() {
        return startValidationAsyncInternal(getPlan());
    }

    @Override
    public void addListener(InvalidationListener listener) {
        sessionChangeProperty().addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        sessionChangeProperty().removeListener(listener);
    }

    /**
     * Internal util method for starting async validation on the {@link #getValidator()}.
     *
     * @param plan the plan to validate
     * @return a promise of a boolean determining the validity
     * @see Validator#isValidAsync(Domain, Problem, Plan)
     * @throws IllegalStateException if validator is null
     */
    private CompletionStage<Boolean> startValidationAsyncInternal(Plan plan) {
        Validator validator = getValidator();
        if (validator == null) {
            throw new IllegalStateException("Cannot validate with null validator.");
        }
        return validator.isValidAsync(getDomain(), getProblem(), plan);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPlanner()).append(getValidator()).append(getDomain()).append(
                getProblem()).append(getPlan()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultPlanningSession)) {
            return false;
        }
        DefaultPlanningSession session = (DefaultPlanningSession) o;
        return new EqualsBuilder().append(getPlanner(), session.getPlanner()).append(getValidator(),
                session.getValidator()).append(getDomain(), session.getDomain()).append(getProblem(),
                session.getProblem()).append(getPlan(), session.getPlan()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("domain", getDomain()).append("problem", getProblem()).append("plan",
                getPlan()).append("planner", getPlanner()).append("validator", getValidator()).toString();
    }
}
