package com.oskopek.transporteditor.planning;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.planner.Planner;
import com.oskopek.transporteditor.planning.problem.Problem;
import com.oskopek.transporteditor.validation.Validator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

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
        return domainProperty.getValue();
    }

    @Override
    public void setDomain(Domain domain) {
        domainProperty.setValue(domain);
    }

    @Override
    public ObjectProperty<Domain> domainProperty() {
        return domainProperty;
    }

    @Override
    public Problem getProblem() {
        return problemProperty.getValue();
    }

    @Override
    public void setProblem(Problem problem) {
        problemProperty.setValue(problem);
    }

    @Override
    public ObjectProperty<Problem> problemProperty() {
        return problemProperty;
    }

    @Override
    public Plan getPlan() {
        return planProperty.getValue();
    }

    @Override
    public void setPlan(Plan plan) {
        planProperty.setValue(plan);
    }

    @Override
    public ObjectProperty<Plan> planProperty() {
        return planProperty;
    }

    @Override
    public Planner getPlanner() {
        return plannerProperty.getValue();
    }

    @Override
    public void setPlanner(Planner planner) {
        plannerProperty.setValue(planner);
    }

    @Override
    public ObjectProperty<Planner> plannerProperty() {
        return plannerProperty;
    }

    @Override
    public Validator getValidator() {
        return validatorProperty.getValue();
    }

    @Override
    public void setValidator(Validator validator) {
        validatorProperty.setValue(validator);
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
}
