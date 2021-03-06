package com.oskopek.transport.validation;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.plan.SequentialPlan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.problem.DefaultProblem;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.SequentialPlanIOIT;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SequentialPlanValidatorTest {

    private SequentialPlanValidator validator;
    private Domain domain;
    private DefaultProblem problem;
    private SequentialPlan plan;

    @BeforeClass
    public static void setUpClass() throws Exception {
        SequentialPlanIOIT.initialize();
    }

    @Before
    public void setUp() throws Exception {
        validator = new SequentialPlanValidator();

        domain = new SequentialDomain("seq");
        problem = TestUtils.P01SequentialProblem();
        plan = TestUtils.P01SequentialPlan();
    }

    @Test
    public void isValidSimple() throws Exception {
        assertThat(validator.isValid(domain, (Problem) problem, plan)).isTrue();
    }

    @Test
    public void isValidSimpleException() throws Exception {
        assertThatThrownBy(() -> validator.isValid(domain, problem, new TemporalPlan(plan.getTemporalPlanActions())))
                .hasMessageContaining("Cannot validate non-sequential plan");
    }

    @Test
    public void isValid() throws Exception {
        assertThat(validator.isValid(domain, problem, plan)).isTrue();
    }

    @Test
    public void isNotValidTruckInInvalidPosition() throws Exception {
        plan.getActions().set(0, domain.buildPickUp(problem.getVehicle("truck-2"),
                problem.getRoadGraph().getLocation("city-loc-4"), problem.getPackage("package-1")));
        assertThat(validator.isValid(domain, problem, plan)).isFalse();
    }

    @Test
    public void isNotValidDropNotPickedUpPackage() throws Exception {
        plan.getActions().remove(0);
        assertThat(validator.isValid(domain, problem, plan)).isFalse();
    }

    @Test
    public void isNotValidPickUpPackageInWrongPlace() throws Exception {
        plan.getActions().add(3, domain.buildPickUp(problem.getVehicle("truck-1"),
                problem.getRoadGraph().getLocation("city-loc-5"), problem.getPackage("package-2")));
        plan.getActions().remove(1);
        assertThat(validator.isValid(domain, problem, plan)).isFalse();
    }

    @Test
    public void isValidEvenWhenPackageNotDelivered() throws Exception {
        plan.getActions().remove(3);
        plan.getActions().remove(0);
        assertThat(validator.isValid(domain, problem, plan)).isTrue();
    }

}
