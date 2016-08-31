/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.VariableDomain;
import fr.uga.pddl4j.parser.Domain;
import fr.uga.pddl4j.parser.Parser;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class VariableDomainIOTest {

    private final String variableDomain_A_PDDL = "variableDomain_A.pddl";
    private final String variableDomain_B_PDDL = "variableDomain_B.pddl";

    private VariableDomain variableDomainA;
    private VariableDomain variableDomainB;

    @Before
    public void setUp() throws Exception {
        variableDomainA = new VariableDomain();

    }

    @Test
    public void serialize() throws Exception {

    }

    @Test
    public void parse() throws Exception {

    }

}