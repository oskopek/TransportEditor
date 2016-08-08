/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.validation;

import static org.junit.Assert.*;
import org.junit.Test;

public class EmptyValidatorTest {

    @Test
    public void isValid() throws Exception {
        assertTrue(new EmptyValidator().isValid(null, null, null));
    }
}
