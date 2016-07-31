package com.oskopek.transporteditor.view;

import static org.junit.Assert.*;
import org.junit.Test;

public class TransportEditorApplicationTest {
    @Test
    public void planningSessionPropertyIsNotNullOnObjectCreation() throws Exception {
        assertNotNull(new TransportEditorApplication().planningSessionProperty());
    }

}
