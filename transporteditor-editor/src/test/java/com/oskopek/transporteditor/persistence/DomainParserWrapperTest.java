/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

public class DomainParserWrapperTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testExploratoryString() throws Exception {
        String simplestProgram = "sphere 12 12 12 cube 2 3 4 cube 4 4 4 sphere 3 3 3";
        CharStream inputCharStream = new ANTLRInputStream(new StringReader(simplestProgram));
        //        TokenSource tokenSource = new DomainLexer(inputCharStream);
        //        TokenStream inputTokenStream = new CommonTokenStream(tokenSource);
        //        DomainParser parser = new DomainParser(inputTokenStream);
        //
        //        parser.addErrorListener(new TestErrorListener());
        //
        //        ProgramContext context = parser.program();
        //
        //        logger.info(context.toString());
    }

    @After
    public void tearDown() throws Exception {

    }

}
