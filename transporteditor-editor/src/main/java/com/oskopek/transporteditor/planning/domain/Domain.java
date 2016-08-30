package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;

import java.util.List;

/**
 * Represents the actual specific Transport domain, with all the constraints, parameters and objective functions
 * fully specified.
 * <p>
 * The domain also knows how to export itself (with all the technicalities) as a valid PDDL file and export (valid)
 * plans created in this domain.
 */
public interface Domain {

    List<? extends Predicate> getPredicates();

    List<? extends Function> getFunctions();

}
