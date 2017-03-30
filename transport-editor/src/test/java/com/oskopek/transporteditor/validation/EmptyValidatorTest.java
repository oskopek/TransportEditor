package com.oskopek.transporteditor.validation;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmptyValidatorTest {

    @Test
    public void isValid() throws Exception {
        assertTrue(new EmptyValidator().isValid(null, null, null));
    }
}
