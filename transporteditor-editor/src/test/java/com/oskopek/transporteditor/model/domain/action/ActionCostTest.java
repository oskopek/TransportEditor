package com.oskopek.transporteditor.model.domain.action;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ActionCostTest {

    @Test
    public void subtract() throws Exception {
        assertEquals(ActionCost.valueOf(1), ActionCost.valueOf(2).subtract(ActionCost.valueOf(1)));
        assertEquals(ActionCost.valueOf(3), ActionCost.valueOf(2).subtract(ActionCost.valueOf(-1)));
    }

    @Test
    public void equalsAndHashCode() {
        assertNotEquals(ActionCost.valueOf(1), null);
        assertNotEquals(null, ActionCost.valueOf(1));
        assertEquals(ActionCost.valueOf(1), ActionCost.valueOf(1));
        assertNotEquals(ActionCost.valueOf(1), ActionCost.valueOf(2));
        assertNotEquals(ActionCost.valueOf(2), ActionCost.valueOf(1));

        assertEquals(ActionCost.valueOf(1).hashCode(), ActionCost.valueOf(1).hashCode());
    }

    @Test
    public void toStringOverriden() {
        assertEquals("0", ActionCost.valueOf(null).toString());
        assertEquals("128", ActionCost.valueOf(128).toString());
    }

}
