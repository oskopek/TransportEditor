package com.oskopek.transport.view.problem;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.DefaultProblemIO;

/**
 * Extension of an XStream-based problem IO to guarantee deserialized graphs are
 * {@link com.oskopek.transport.model.problem.graph.VisualRoadGraph}s.
 */
public class VisualProblemIO extends DefaultProblemIO {

    /**
     * Default constructor.
     *
     * @param domain the domain
     */
    public VisualProblemIO(Domain domain) {
        super(domain);
    }

    @Override
    public VisualProblem parse(String contents) {
        Problem problem = super.parse(contents);
        return new VisualProblem(problem.getName(), new DefaultVisualRoadGraph(problem.getRoadGraph()),
                problem.getVehicleMap(), problem.getPackageMap());
    }
}
