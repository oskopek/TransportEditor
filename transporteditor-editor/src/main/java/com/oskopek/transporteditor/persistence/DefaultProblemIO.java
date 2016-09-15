/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.HashMap;
import java.util.Map;

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
        PddlParser parser = new PddlParser(new CommonTokenStream(new PddlLexer(new ANTLRInputStream(contents))));
        ErrorDetectionListener listener = new ErrorDetectionListener();
        parser.addErrorListener(listener);
        PddlParser.ProblemContext context = parser.problem();
        if (!context.problemDomain().NAME().getText().equals("transport")) {
            throw new IllegalArgumentException("Domain is not a transport domain!");
        }

        String name = context.problemDecl().getText();
        Map<String, Vehicle> vehicleMap = new HashMap<>();
        Map<String, Package> packageMap = new HashMap<>();
        RoadGraph graph = new RoadGraph(name + "_graph");
        int maxCapacity = -1;

        for (PddlParser.SingleTypeNameListContext typeNameListContext : context.objectDecl().typedNameList()
                .singleTypeNameList()) {
            String typeName = typeNameListContext.type().getText();
            String objectName = typeNameListContext.NAME(0).getText();
            switch (typeName) {
                case "vehicle":
                    vehicleMap.put(objectName, new Vehicle(objectName, null, null, null, null));
                    break;
                case "package":
                    packageMap.put(objectName, new Package(objectName, null, null, null));
                    break;
                case "location":
                    graph.addLocation(new Location(objectName, null, null));
                    break;
                case "capacity-number":
                    String[] split = objectName.split("-");
                    if (split.length != 2) {
                        throw new IllegalArgumentException("Invalid capacity-number value: " + objectName);
                    }
                    int capacity = Integer.parseInt(split[1]);
                    if (capacity > maxCapacity) {
                        maxCapacity = capacity;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid type: " + typeName);
            }
        }

        // TODO: init section, goal section, metric section

        return new DefaultProblem(name, graph, vehicleMap, packageMap);
    }
}
