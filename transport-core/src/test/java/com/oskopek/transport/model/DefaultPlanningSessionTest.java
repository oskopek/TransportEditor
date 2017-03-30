package com.oskopek.transport.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultPlanningSessionTest {

    private PlanningSession session;

    @Before
    public void setUp() throws Exception {
        session = new DefaultPlanningSession();
    }

    @Test
    public void shouldHaveNonNullProperties() throws Exception {
        Assert.assertNotNull(session.plannerProperty());
        Assert.assertNotNull(session.validatorProperty());
        Assert.assertNotNull(session.domainProperty());
        Assert.assertNotNull(session.problemProperty());
        Assert.assertNotNull(session.planProperty());

    }

}
