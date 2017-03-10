package com.oskopek.transporteditor.validation;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.SequentialPlanIOIT;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

public class SequentialPlanValidatorIT {

    private final SequentialPlanValidator validator = new SequentialPlanValidator();
    private final SequentialDomain domain = new SequentialDomain("seq");

    @Before
    public void setUp() throws Exception {
        SequentialPlanIOIT.initialize();
    }

    @Test
    public void testP20HeuristicPlan() throws Exception {
        Domain domain = this.domain;
        assumeNotNull(domain);
        Problem problem = SequentialPlanIOIT.p20Problem;
        assumeNotNull(problem);
        Plan plan = new SequentialPlanIO(domain, problem).parse(SequentialPlanIOIT.P20SequentialPlanFileContents);
        assumeNotNull(plan);

        assertThat(validator.isValid(domain, problem, plan)).isTrue();
    }
}
