package com.oskopek.transporteditor.planning.plan;

import java.util.Collection;

/**
 * Represents a plan, for a specific domain.
 * Should not have any knowledge of the domain,
 * should just be a sequence of planning objects.
 * <p>
 * Represents both time aware and non-temporal plans.
 */
public interface Plan {

    Collection<? extends PlanEntry> getPlanEntries();

}
