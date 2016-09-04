/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import static org.junit.Assert.*;
import org.junit.Test;

public class DefaultLocatableTest {
    @Test
    public void nonNullProperties() throws Exception {
        Locatable locatable = new DefaultLocatable(null, null);
        assertNotNull(locatable.nameProperty());
        assertNotNull(locatable.locationProperty());
    }
}
