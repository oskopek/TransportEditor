package com.oskopek.transporteditor.plan;

import org.junit.Assert;
import org.junit.Test;


public class DefaultPlanningSessionTest {

    @Test(expected = RuntimeException.class)
    public void shouldAlwaysHaveDomain() throws Exception {
        PlanningSession session = new DefaultPlanningSession();
        Assert.assertNotNull(session.getDomain());
    }
}
