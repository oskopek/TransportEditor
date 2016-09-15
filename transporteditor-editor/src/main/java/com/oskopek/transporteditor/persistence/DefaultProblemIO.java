/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class DefaultProblemIO implements DataReader<DefaultProblem>, DataWriter<DefaultProblem> {

    private final Domain domain;

    public DefaultProblemIO(Domain domain) {
        this.domain = domain;
    }

    @Override
    public String serialize(DefaultProblem object) throws IllegalArgumentException {
        return null;
    }

    @Override
    public DefaultProblem parse(String contents) throws IllegalArgumentException {
        ProblemParser parser = new ProblemParser(
                new CommonTokenStream(new ProblemLexer(new ANTLRInputStream(contents))));
        ErrorDetectionListener listener = new ErrorDetectionListener();
        parser.addErrorListener(listener);
        ProblemParser.ProblemContext context = parser.problem();

        DefaultProblem problem = new DefaultProblem(null, null, null);
        return problem;
    }
}
