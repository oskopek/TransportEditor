package com.oskopek.transport.model.domain.action;

import org.junit.Test;

import static org.junit.Assert.*;

public class ActionCostTest {

    @Test
    public void subtract() throws Exception {
        assertEquals(ActionCost.ONE, ActionCost.valueOf(2).subtract(ActionCost.ONE));
        assertEquals(ActionCost.valueOf(3), ActionCost.valueOf(2).subtract(ActionCost.valueOf(-1)));
    }

    @Test
    public void equalsAndHashCode() {
        assertNotEquals(ActionCost.ONE, null);
        assertNotEquals(null, ActionCost.ONE);
        assertEquals(ActionCost.valueOf(1), ActionCost.ONE);
        assertNotEquals(ActionCost.ONE, ActionCost.valueOf(2));
        assertNotEquals(ActionCost.valueOf(2), ActionCost.ONE);

        assertEquals(ActionCost.valueOf(1).hashCode(), ActionCost.ONE.hashCode());
    }

    @Test
    public void toStringOverriden() {
        assertEquals("0", ActionCost.valueOf(null).toString());
        assertEquals("128", ActionCost.valueOf(128).toString());
    }

}
