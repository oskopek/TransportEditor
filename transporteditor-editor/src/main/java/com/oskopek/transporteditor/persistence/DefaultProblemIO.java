/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
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
        return "";
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

        ParsedProblemContainer parsed = new ParsedProblemContainer(context.problemDecl().getText());
        parseObjectDecl(context.objectDecl(), parsed);
        parseInit(context.init(), parsed);
        parseGoalDescContext(context.goal(), parsed);
        return new DefaultProblem(parsed.name(), parsed.graph(), parsed.vehicleMap(), parsed.packageMap());
    }

    private void parseInit(PddlParser.InitContext initContext, ParsedProblemContainer parsed) {
        for (PddlParser.InitElContext initElContext : initContext.initEl()) {
            if (initElContext.nameLiteral() != null) {
                String predicate = initElContext.nameLiteral().atomicNameFormula().predicate().getText();
                String arg1 = initElContext.nameLiteral().atomicNameFormula().NAME(0).getText();

                if ("has-petrol-station".equals(predicate)) {
                    parsed.graph().setPetrolStation(parsed.graph().getLocation(arg1));
                    continue;
                } else if ("ready-loading".equals(predicate)) {
                    // ignore predicate for now
                    continue;
                }

                String arg2 = initElContext.nameLiteral().atomicNameFormula().NAME(1).getText();
                switch (predicate) {
                    case "at": {
                        Vehicle vehicle = parsed.vehicleMap().get(arg1);
                        if (vehicle != null) {
                            Vehicle newVehicle = new Vehicle(vehicle.getName(), parsed.graph().getLocation(arg2),
                                    vehicle.getCurCapacity(), vehicle.getMaxCapacity(), vehicle.getPackageList());
                            parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                            break;
                        }
                        Package pkg = parsed.packageMap().get(arg1);
                        if (pkg != null) {
                            Package newpkg = new Package(pkg.getName(), parsed.graph().getLocation(arg2),
                                    pkg.getTarget(),
                                    pkg.getSize());
                            parsed.packageMap().put(newpkg.getName(), newpkg);
                            break;
                        }
                        break;
                    }
                    case "capacity": {
                        Vehicle vehicle = parsed.vehicleMap().get(arg1);
                        if (vehicle != null) {
                            ActionCost capacity = ActionCost.valueOf(Integer.parseInt(arg2.split("-")[1]));
                            Vehicle newVehicle = new Vehicle(vehicle.getName(), vehicle.getLocation(), capacity,
                                    capacity, vehicle.getPackageList());
                            parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                            break;
                        }
                        break;
                    }
                    case "road": {
                        Location from = parsed.graph().getLocation(arg1);
                        Location to = parsed.graph().getLocation(arg2);
                        Road road = DefaultRoad.build(from, to);
                        parsed.graph().addRoad(road, from, to);
                        break;
                    }
                    default:
                        break; // ignore other predicates
                }
            }
            if (initElContext.fHead() != null) {
                PddlParser.FHeadContext fhead = initElContext.fHead();
                int number = Integer.parseInt(initElContext.NUMBER().getText());
                PddlParser.FunctionSymbolContext functionSymbol = fhead.functionSymbol();
                if (functionSymbol == null) {
                    continue;
                }
                switch (functionSymbol.getText()) {
                    case "road-length": {
                        String fromName = fhead.term(0).getText();
                        String toName = fhead.term(1).getText();
                        Location from = parsed.graph().getLocation(fromName);
                        Location to = parsed.graph().getLocation(toName);
                        Road newRoad = DefaultRoad.build(from, to, ActionCost.valueOf(number));
                        parsed.graph().putRoad(newRoad, from, to);
                        break;
                    }
                    case "capacity": {
                        String vehicleName = fhead.term(0).getText();
                        Vehicle vehicle = parsed.vehicleMap().get(vehicleName);
                        if (vehicle != null) {
                            ActionCost capacity = ActionCost.valueOf(number);
                            Vehicle newVehicle = new Vehicle(vehicle.getName(), vehicle.getLocation(), capacity,
                                    capacity, vehicle.getPackageList());
                            parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                            break;
                        }
                        break;
                    }
                    case "fuel-left": {
                        String vehicleName = fhead.term(0).getText();
                        Vehicle vehicle = parsed.vehicleMap().get(vehicleName);
                        if (vehicle != null) {
                            ActionCost fuelLeft = ActionCost.valueOf(number);
                            ActionCost fuelMax = null;
                            if (FuelVehicle.class.isAssignableFrom(vehicle.getClass())) {
                                fuelMax = ((FuelVehicle) vehicle).getMaxFuelCapacity();
                            }
                            FuelVehicle newVehicle = new FuelVehicle(vehicle.getName(), vehicle.getLocation(),
                                    vehicle.getCurCapacity(), vehicle.getMaxCapacity(), vehicle.getPackageList(),
                                    fuelLeft, fuelMax);
                            parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                            break;
                        }
                        break;
                    }
                    case "fuel-max": {
                        String vehicleName = fhead.term(0).getText();
                        Vehicle vehicle = parsed.vehicleMap().get(vehicleName);
                        if (vehicle != null) {
                            ActionCost fuelMax = ActionCost.valueOf(number);
                            ActionCost fuelLeft = null;
                            if (FuelVehicle.class.isAssignableFrom(vehicle.getClass())) {
                                fuelLeft = ((FuelVehicle) vehicle).getCurFuelCapacity();
                            }
                            FuelVehicle newVehicle = new FuelVehicle(vehicle.getName(), vehicle.getLocation(),
                                    vehicle.getCurCapacity(), vehicle.getMaxCapacity(), vehicle.getPackageList(),
                                    fuelLeft, fuelMax);
                            parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                            break;
                        }
                        break;
                    }
                    case "fuel-demand": {
                        String fromName = fhead.term(0).getText();
                        String toName = fhead.term(1).getText();
                        Location from = parsed.graph().getLocation(fromName);
                        Location to = parsed.graph().getLocation(toName);
                        Road road = parsed.graph().getRoadBetween(from, to);
                        parsed.graph().putRoad(FuelRoad.build(road, ActionCost.valueOf(number)), from, to);
                        break;
                    }
                    default:
                        break; // ignore other functions
                }
            }
        }
    }

    private void parseObjectDecl(PddlParser.ObjectDeclContext objectDecl, ParsedProblemContainer parsed) {
        int maxCapacity = -1;
        for (PddlParser.SingleTypeNameListContext typeNameListContext : objectDecl.typedNameList()
                .singleTypeNameList()) {
            String typeName = typeNameListContext.type().getText();
            String objectName = typeNameListContext.NAME(0).getText();
            switch (typeName) {
                case "vehicle":
                    parsed.vehicleMap().put(objectName, new Vehicle(objectName, null, null, null, null));
                    break;
                case "package":
                    parsed.packageMap().put(objectName, new Package(objectName, null, null, null));
                    break;
                case "location":
                    parsed.graph().addLocation(new Location(objectName, 0, 0));
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
    }

    private void parseGoalDescContext(PddlParser.GoalContext goalContext, ParsedProblemContainer parsed) {
        for (PddlParser.GoalDescContext goalDescContext : goalContext.goalDesc().goalDesc()) { // we add 'and' implictly
            String predicate = goalDescContext.atomicTermFormula().predicate().getText();
            String arg1 = goalDescContext.atomicTermFormula().term(0).getText();
            String arg2 = goalDescContext.atomicTermFormula().term(1).getText();
            switch (predicate) {
                case "at": {
                    Package pkg = parsed.packageMap().get(arg1);
                    Location target = parsed.graph().getLocation(arg2);
                    if (pkg != null) {
                        Package newpkg = new Package(pkg.getName(), pkg.getLocation(), target, pkg.getSize());
                        parsed.packageMap().put(newpkg.getName(), newpkg);
                        break;
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Invalid predicate in goalDesc: " + predicate);
            }
        }
    }

    private static final class ParsedProblemContainer {

        private final String name;
        private final Map<String, Vehicle> vehicleMap = new HashMap<>();
        private final Map<String, Package> packageMap = new HashMap<>();
        private final RoadGraph graph;

        ParsedProblemContainer(String name) {
            this.name = name;
            graph = new RoadGraph(name + "_graph");
        }

        public String name() {
            return name;
        }

        public Map<String, Vehicle> vehicleMap() {
            return vehicleMap;
        }

        public Map<String, Package> packageMap() {
            return packageMap;
        }

        public RoadGraph graph() {
            return graph;
        }
    }
}
