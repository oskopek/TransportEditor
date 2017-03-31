package com.oskopek.transport.model.domain.action.predicates;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.TemporalQuantifier;
import com.oskopek.transport.model.problem.Problem;

/**
 * A PDDL prediceate representation.
 */
public interface Predicate {

    /**
     * Returns the validity of the predicate in the given problem state. The action object should only be used
     * for passing in parameters for the validation. This method should be pure.
     *
     * @param state the state to validate predicate in
     * @param action the parameters to use for validation
     * @return true iff the predicate is valid in the state for given parameters
     */
    boolean isValid(Problem state, Action action);

    /**
     * Get the temporal quantifier, a modifier to check the predicate at specified action positions.
     *
     * @return the temporal quantifier
     */
    default TemporalQuantifier getTemporalQuantifier() {
        return TemporalQuantifier.OVER_ALL;
    }

    /**
     * Get the predicate name, for debugging purposes.
     *
     * @return the predicate name.
     */
    default String getPredicateName() {
        return getClass().getSimpleName() + "[" + getTemporalQuantifier().name() + "]";
    }

}
