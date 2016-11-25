package com.oskopek.transporteditor.model;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.validation.Validator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The default implementation of a planning session.
 */
public class DefaultPlanningSession implements PlanningSession {

    private final ObjectProperty<Planner> plannerProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Validator> validatorProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Domain> domainProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Problem> problemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Plan> planProperty = new SimpleObjectProperty<>();

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
    public void startPlanning() {
        getPlanner().startPlanning(getDomain(), getProblem());
    }

    @Override
    public void stopPlanning() {
        getPlanner().stopPlanning();
        getValidator().isValid(getDomain(), getProblem(), getPlanner().getBestPlan());
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
}
