package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.plan.Plan;

import java.io.PrintWriter;
import java.util.List;

/**
 * Represents the actual specific Transport domain, with all the constraints, parameters and objective functions
 * fully specified.
 * <p>
 * The domain also knows how to export itself (with all the technicalities) as a valid PDDL file and export (valid)
 * plans created in this domain.
 */
public interface Domain {

    List<Predicate> getPredicates();

    List<Function> getFunctions();

    void toPDDLFormat(PrintWriter writer);

    Domain fromPDDLFormat(String input); // does this actually make sense?

    void toVALFormat(PrintWriter writer, Plan plan);

    Plan fromVALFormat(String input);

}
