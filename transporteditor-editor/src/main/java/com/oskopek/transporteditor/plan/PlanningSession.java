package com.oskopek.transporteditor.plan;

import com.oskopek.transporteditor.domain.Domain;

/**
 * Represents an orchestrator of the current planning session.
 * <p>
 * Keeps track of the domain, the plan, planner and any other user session
 * related data and settings (planner args, etc).
 */
public interface PlanningSession {

    /**
     * Manages a reference to the Transport domain variant used for planning in this session.
     *
     * @return the associated domain
     */
    Domain getDomain();

}
