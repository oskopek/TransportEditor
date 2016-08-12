/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import static org.junit.Assert.*;
import org.junit.Test;

public class PackageTest {
    @Test
    public void nonNullProperties() throws Exception {
        Package pkg = new Package(null, null, null);
        assertNotNull(pkg.nameProperty());
        assertNotNull(pkg.locationProperty());
        assertNotNull(pkg.targetProperty());
    }
}
