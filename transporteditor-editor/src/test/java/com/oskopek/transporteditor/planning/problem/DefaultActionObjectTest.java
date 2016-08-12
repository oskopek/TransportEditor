/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import static org.junit.Assert.*;
import org.junit.Test;

public class DefaultActionObjectTest {
    @Test
    public void nameProperty() throws Exception {
        assertNotNull(new DefaultActionObject(null).nameProperty());
    }
}
