package com.oskopek.transport.validation;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmptyValidatorTest {

    @Test
    public void isValid() throws Exception {
        assertTrue(new EmptyValidator().isValid(null, null, null));
    }
}
