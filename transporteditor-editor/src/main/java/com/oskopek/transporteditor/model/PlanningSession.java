package com.oskopek.transporteditor.model;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.validation.Validator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;

import java.util.concurrent.CompletionStage;

/**
 * Represents an orchestrator of the current planning session.
 * <p>
 * Keeps track of the domain, the plan, planner and any other user session
 * related data and settings (planner args, etc).
 */
public interface PlanningSession {

    /**
     * Get the domain.
     *
     * @return the domain
     */
    Domain getDomain();

    /**
     * Set the domain.
     *
     * @param domain the domain to set
     */
    void setDomain(Domain domain);

    /**
     * Get the domain property.
     *
     * @return the domain property
     */
    ObjectProperty<Domain> domainProperty();

    /**
     * Get the problem.
     *
     * @return the problem
     */
    Problem getProblem();

    /**
     * Set the problem.
     *
     * @param problem the problem to set
     */
    void setProblem(Problem problem);

    /**
     * Get the problem property.
     *
     * @return the problem property
     */
    ObjectProperty<Problem> problemProperty();

    /**
     * Get the plan.
     *
     * @return the plan
     */
    Plan getPlan();

    /**
     * Set the plan.
     *
     * @param plan the plan to set
     */
    void setPlan(Plan plan);

    /**
     * Get the plan property.
     *
     * @return the plan property
     */
    ObjectProperty<Plan> planProperty();

    /**
     * Get the planner.
     *
     * @return the planner
     */
    Planner getPlanner();

    /**
     * Set the planner.
     *
     * @param planner the planner to set
     */
    void setPlanner(Planner planner);

    /**
     * Get the planner property.
     *
     * @return the planner property
     */
    ObjectProperty<Planner> plannerProperty();

    /**
     * Get the validator.
     *
     * @return the validator
     */
    Validator getValidator();

    /**
     * Set the validator.
     *
     * @param validator the validator to set
     */
    void setValidator(Validator validator);

    /**
     * Get the validator property.
     *
     * @return the validator property
     */
    ObjectProperty<Validator> validatorProperty();

    /**
     * Run planning asynchronously using the session's properties and set the result back into the session.
     *
     * @return a promise of the plan
     * @throws IllegalStateException if planner is null
     * @see Planner#startAsync(Domain, Problem)
     */
    CompletionStage<Plan> startPlanningAsync();

    /**
     * Run validation asynchronously on the session's plan.
     *
     * @return a promise of an "is valid" boolean
     * @throws IllegalStateException if validator is null
     * @see Validator#isValidAsync(Domain, Problem, Plan)
     */
    CompletionStage<Boolean> startValidationAsync();

    /**
     * Adds a listener to listening to session changes.
     *
     * @param listener the listener to add
     */
    void addListener(InvalidationListener listener);

    /**
     * Remove a listener from listening to session changes.
     *
     * @param listener the listener to remove
     */
    void removeListener(InvalidationListener listener);

}
