package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.*;
import com.oskopek.transporteditor.model.problem.Package;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import javaslang.Tuple;
import javaslang.Tuple3;
import javaslang.collection.Array;
import javaslang.collection.Seq;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultProblemIO implements DataReader<DefaultProblem>, DataWriter<DefaultProblem> {

    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);

    static {
        configuration.setClassForTemplateLoading(DefaultProblemIO.class, "");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    private final Domain domain;

    public DefaultProblemIO(Domain domain) {
        this.domain = domain;
    }

    @Override
    public String serialize(DefaultProblem object) throws IllegalArgumentException {
        Map<String, Object> input = new HashMap<>();
        input.put("date", new Date());
        input.put("title", object.getName().replaceFirst("transport-", ""));
        input.put("domain", domain);
        input.put("problem", object);
        input.put("packageList",
                object.getAllPackages().stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                        .collect(Collectors.toList()));
        input.put("locationList",
                object.getRoadGraph().getAllLocations().sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                        .collect(Collectors.toList()));
        input.put("vehicleList",
                object.getAllVehicles().stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                        .collect(Collectors.toList()));
        Seq<Location> allLocations = object.getRoadGraph().getAllLocations().collect(Array.collector());
        List<Tuple3<Location, Location, Road>> roads = allLocations.crossProduct().map(
                t -> Tuple.of(t._1, t._2, object.getRoadGraph().getRoadBetween(t._1, t._2))).filter(t -> t._3 != null)
                .toJavaList();
        input.put("roads", roads);
        input.put("petrolLocationList", allLocations.filter(object.getRoadGraph()::hasPetrolStation).toJavaList());
        Optional<Integer> maxCapacityOpt = object.getAllVehicles().stream().map(Vehicle::getMaxCapacity).map(
                ActionCost::getCost).collect(Collectors.maxBy(Integer::compare));
        int maxCapacity = 0;
        if (maxCapacityOpt.isPresent()) {
            maxCapacity = maxCapacityOpt.get();
        }
        input.put("maxCapacity", maxCapacity);
        input.put("actionCost", PddlLabel.ActionCost);
        input.put("numeric", PddlLabel.Numeric);
        input.put("temporal", PddlLabel.Temporal);

        Template template;
        try {
            template = configuration.getTemplate("problem.pddl.ftl");
        } catch (IOException e) {
            throw new IllegalStateException("Error occurred during reading template file.", e);
        }

        StringWriter writer = new StringWriter();
        try {
            template.process(input, writer);
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Error occurred during processing template.", e);
        }
        return writer.toString().replaceAll("\\r\\n", "\n");
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

        ParsedProblemContainer parsed = new ParsedProblemContainer(context.problemDecl().NAME().getText());
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
                    case "package-size": {
                        String packageName = fhead.term(0).getText();
                        Package pkg = parsed.packageMap().get(packageName);
                        if (pkg != null) {
                            ActionCost size = ActionCost.valueOf(number);
                            Package newPackage = new Package(pkg.getName(), pkg.getLocation(), pkg.getTarget(), size);
                            parsed.packageMap().put(newPackage.getName(), newPackage);
                            break;
                        }
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
                            if (Vehicle.class.isAssignableFrom(vehicle.getClass())) {
                                fuelMax = vehicle.getMaxFuelCapacity();
                            }
                            Vehicle newVehicle = new Vehicle(vehicle.getName(), vehicle.getLocation(),
                                    vehicle.getCurCapacity(), vehicle.getMaxCapacity(),
                                    fuelLeft, fuelMax, vehicle.getPackageList());
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
                            if (Vehicle.class.isAssignableFrom(vehicle.getClass())) {
                                fuelLeft = vehicle.getCurFuelCapacity();
                            }
                            Vehicle newVehicle = new Vehicle(vehicle.getName(), vehicle.getLocation(),
                                    vehicle.getCurCapacity(), vehicle.getMaxCapacity(),
                                    fuelLeft, fuelMax, vehicle.getPackageList());
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
