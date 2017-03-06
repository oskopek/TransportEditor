package com.oskopek.transporteditor.model.domain.action;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Problem;

import java.util.List;

/**
 * Represents an action (non-temporal) in a plan of the Transport domain.
 */
public interface Action {

    /**
     * Get the name of the action.
     *
     * @return the name
     */
    String getName();

    /**
     * Get who is doing the action.
     *
     * @return the who
     */
    Locatable getWho();

    /**
     * Get the location where the action is taking place.
     *
     * @return the where
     */
    Location getWhere();

    /**
     * Get the object that is the subject of the action, what.
     *
     * @return the what
     */
    ActionObject getWhat();

    /**
     * Get the preconditions.
     *
     * @return the preconditions
     */
    List<Predicate> getPreconditions();

    /**
     * Get the effects.
     *
     * @return the effects
     */
    List<Predicate> getEffects();

    /**
     * Get the cost.
     *
     * @return the cost
     */
    ActionCost getCost();

    /**
     * Get the duration.
     *
     * @return the duration
     */
    ActionCost getDuration();

    /**
     * Apply this action to a problem, returning a changed problem.
     *
     * @param domain the domain of the problem
     * @param problemState the state before applying
     * @return the state after applying
     */
    default Problem apply(Domain domain, Problem problemState) {
        return applyEffects(domain, applyPreconditions(domain, problemState));
    }

    Problem applyPreconditions(Domain domain, Problem problemState);

    Problem applyEffects(Domain domain, Problem problemState);

    /**
     * Check if all preconditions of the action are valid in the given state.
     *
     * @param state the state to validate
     * @return true iff all preconditions are valid in the given state
     */
    default boolean arePreconditionsValid(Problem state) {
        return getPreconditions().stream().map(p -> p.isValid(state, this)).reduce(true, Boolean::logicalAnd);
    }

    /**
     * Check if all effects of the action are valid in the given state.
     *
     * @param state the state to validate
     * @return true iff all effects are valid in the given state
     */
    default boolean areEffectsValid(Problem state) {
        return getEffects().stream().map(p -> p.isValid(state, this)).reduce(true, Boolean::logicalAnd);
    }

}
