package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.Drop;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TemporalPlanTest {

    private TemporalPlan plan;

    @Before
    public void setUp() throws Exception {
        Set<TemporalPlanAction> planActionSet = new HashSet<>();
        planActionSet
                .add(new TemporalPlanAction(new PickUp(null, null, null, null, null, null, ActionCost.valueOf(1)), 0d,
                        1d));
        planActionSet
                .add(new TemporalPlanAction(new PickUp(null, null, null, null, null, null, ActionCost.valueOf(2)), 0d,
                        2d));
        planActionSet
                .add(new TemporalPlanAction(new Drop(null, null, null, null, null, null, ActionCost.valueOf(1)), 1d, 2d));
        planActionSet
                .add(new TemporalPlanAction(new Drop(null, null, null, null, null, null, ActionCost.valueOf(2)), 1d, 3d));
        plan = new TemporalPlan(planActionSet);
    }

    @Test
    public void getActionsAt() throws Exception {
        assertEquals(0, plan.getActionsAt(-1).size());
        assertEquals(2, plan.getActionsAt(0).size());
        assertEquals(3, plan.getActionsAt(1).size());
        assertEquals(1, plan.getActionsAt(2).size());
        assertEquals(0, plan.getActionsAt(3).size());
        assertTrue(plan.getActionsAt(2).contains(new Drop(null, null, null, null, null, null, ActionCost.valueOf(2))));
        assertEquals(0, plan.getActionsAt(4).size());
    }

    @Test
    public void getTemporalActionsAt() throws Exception {
        assertEquals(0, plan.getTemporalActionsAt(-1).size());
        assertEquals(2, plan.getTemporalActionsAt(0).size());
        assertEquals(3, plan.getTemporalActionsAt(1).size());
        assertEquals(1, plan.getTemporalActionsAt(2).size());
        assertEquals(0, plan.getTemporalActionsAt(3).size());
        assertTrue(plan.getTemporalActionsAt(2).contains(
                new TemporalPlanAction(new Drop(null, null, null, null, null, null, ActionCost.valueOf(2)), 1d, 3d)));
        assertEquals(0, plan.getTemporalActionsAt(4).size());
    }

    @Test
    public void getTemporalPlanActions() throws Exception {
        assertEquals(4, plan.getTemporalPlanActions().size());
    }

    @Test
    public void getAllActions() throws Exception {
        assertEquals(4, plan.getActions().size());
    }

}
