package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.persistence.antlr4.PddlLexer;
import com.oskopek.transport.persistence.antlr4.PddlParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reader and writer for {@link DefaultProblem} to and from PDDL (supports only Transport domains).
 * Uses a Freemarker template internally for serialization and ANTLR for deserialization.
 */
public class DefaultProblemIO implements DataIO<Problem> {

    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);

    static {
        configuration.setClassForTemplateLoading(DefaultProblemIO.class, "");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final Domain domain;

    /**
     * Default constructor.
     *
     * @param domain the domain of the problem read and written
     */
    public DefaultProblemIO(Domain domain) {
        this.domain = domain;
    }

    @Override
    public String serialize(Problem object) {
        Map<String, Object> input = new HashMap<>(30);
        input.put("date", new Date());
        input.put("title", object.getName().replaceFirst("transport-", ""));
        input.put("domain", domain);
        input.put("problem", object);
        input.put("packageList",
                object.getAllPackages().stream().sorted(Comparator.comparing(DefaultActionObject::getName))
                        .collect(Collectors.toList()));
        input.put("locationList",
                object.getRoadGraph().getAllLocations().sorted(Comparator.comparing(Location::getName))
                        .collect(Collectors.toList()));
        input.put("vehicleList",
                object.getAllVehicles().stream().sorted(Comparator.comparing(DefaultActionObject::getName))
                        .collect(Collectors.toList()));
        Seq<Location> allLocations = object.getRoadGraph().getAllLocations().collect(Array.collector());
        List<Tuple3<Location, Location, Road>> roads = allLocations.crossProduct().map(
                t -> Tuple.of(t._1, t._2, object.getRoadGraph().getShortestRoadBetween(t._1, t._2)))
                .filter(t -> t._3 != null).toJavaList();
        input.put("roads", roads);
        input.put("petrolLocationList", allLocations.filter(Location::hasPetrolStation).toJavaList());
        Optional<Integer> maxCapacityOpt = object.getAllVehicles().stream().map(Vehicle::getMaxCapacity).map(
                ActionCost::getCost).max(Integer::compare);
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

    /**
     * Parse a default problem from a string.
     *
     * @param contents the string representation of the problem
     * @return an initialized instance of the problem object
     * @throws IllegalArgumentException if the contents cannot be parsed
     */
    public DefaultProblem parseDefault(String contents) {
        PddlParser parser = new PddlParser(new CommonTokenStream(new PddlLexer(new ANTLRInputStream(contents))));
        ErrorDetectionListener listener = new ErrorDetectionListener();
        parser.addErrorListener(listener);
        PddlParser.ProblemContext context = parser.problem();
        if (listener.isFail()) {
            Exception reason = listener.getReason();
            if (reason != null) {
                throw new IllegalArgumentException("Failed to parse problem pddl: " + listener.getReasonString(),
                        reason);
            } else {
                throw new IllegalArgumentException("Failed to parse problem pddl.");
            }
        }
        if (!context.problemDomain().NAME().getText().equals("transport")) {
            throw new IllegalArgumentException("Domain is not a transport domain!");
        }

        ParsedProblemContainer parsed = new ParsedProblemContainer(context.problemDecl().NAME().getText());
        parseObjectDecl(context.objectDecl(), parsed);
        parseInit(context.init(), parsed);
        parseGoalDescContext(context.goal(), parsed);
        return new DefaultProblem(parsed.name(), parsed.graph(), parsed.vehicleMap(), parsed.packageMap());
    }


    @Override
    public Problem parse(String contents) {
        return parseDefault(contents);
    }

    /**
     * Parse the {@code (:init ...)} context.
     *
     * @param initContext the context to parse
     * @param parsed intermediate problem properties aggregator
     */
    private void parseInit(PddlParser.InitContext initContext, ParsedProblemContainer parsed) {
        for (PddlParser.InitElContext initElContext : initContext.initEl()) {
            if (initElContext.nameLiteral() != null) {
                parsePredicate(initElContext, parsed);
            }
            if (initElContext.fHead() != null) {
                parseFunction(initElContext, parsed);
            }
        }
    }

    /**
     * Parse a predicate of the {@code (:init ...)} context.
     *
     * @param initElContext the context to parse
     * @param parsed intermediate problem properties aggregator
     */
    private void parsePredicate(PddlParser.InitElContext initElContext, ParsedProblemContainer parsed) {
        String predicate = initElContext.nameLiteral().atomicNameFormula().predicate().getText();
        String arg1 = initElContext.nameLiteral().atomicNameFormula().NAME(0).getText();

        if ("has-petrol-station".equals(predicate)) {
            if (!domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                logger.debug("Fuel fuel-related predicate ({}) in non-fuel domain, skipping.", predicate);
                return;
            }
            parsed.graph().setPetrolStation(arg1, true);
            return;
        } else if ("ready-loading".equals(predicate)) {
            // ignore predicate for now
            return;
        }

        String arg2 = initElContext.nameLiteral().atomicNameFormula().NAME(1).getText();
        switch (predicate) {
            case "at": {
                Vehicle vehicle = parsed.vehicleMap().get(arg1);
                if (vehicle != null) {
                    Vehicle newVehicle = vehicle.updateLocation(parsed.graph().getLocation(arg2));
                    parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                    break;
                }
                Package pkg = parsed.packageMap().get(arg1);
                if (pkg != null) {
                    Package newpkg = pkg.updateLocation(parsed.graph().getLocation(arg2));
                    parsed.packageMap().put(newpkg.getName(), newpkg);
                    break;
                }
                break;
            }
            case "capacity": {
                Vehicle vehicle = parsed.vehicleMap().get(arg1);
                if (vehicle != null) {
                    ActionCost capacity = ActionCost.valueOf(Integer.parseInt(arg2.split("-")[1]));
                    Vehicle newVehicle = vehicle.updateCurCapacity(capacity).updateMaxCapacity(capacity);
                    parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                    break;
                }
                break;
            }
            case "road": {
                Location from = parsed.graph().getLocation(arg1);
                Location to = parsed.graph().getLocation(arg2);
                Road road;
                if (domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                    road = FuelRoad.build(from, to);
                } else {
                    road = DefaultRoad.build(from, to);
                }
                parsed.graph().addRoad(road, from, to);
                break;
            }
            default:
                break; // ignore other predicates
        }
    }

    /**
     * Parse a function of the {@code (:init ...)} context.
     *
     * @param initElContext the context to parse
     * @param parsed intermediate problem properties aggregator
     */
    private void parseFunction(PddlParser.InitElContext initElContext, ParsedProblemContainer parsed) {
        PddlParser.FHeadContext fhead = initElContext.fHead();
        int number = Integer.parseInt(initElContext.NUMBER().getText());
        PddlParser.FunctionSymbolContext functionSymbol = fhead.functionSymbol();
        if (functionSymbol == null) {
            return;
        }
        switch (functionSymbol.getText()) {
            case "road-length": {
                String fromName = fhead.term(0).getText();
                String toName = fhead.term(1).getText();
                Location from = parsed.graph().getLocation(fromName);
                Location to = parsed.graph().getLocation(toName);
                Road newRoad = parsed.graph().getShortestRoadBetween(from, to)
                        .updateLength(ActionCost.valueOf(number));
                parsed.graph().putRoad(newRoad, from, to);
                break;
            }
            case "package-size": {
                String packageName = fhead.term(0).getText();
                Package pkg = parsed.packageMap().get(packageName);
                if (pkg != null) {
                    ActionCost size = ActionCost.valueOf(number);
                    Package newPackage = pkg.updateSize(size);
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
                    Vehicle newVehicle = vehicle.updateCurCapacity(capacity).updateMaxCapacity(capacity);
                    parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                    break;
                }
                break;
            }
            case "fuel-left": {
                if (!domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                    logger.debug("Fuel fuel-related function ({}) in non-fuel domain, skipping.",
                            functionSymbol.getText());
                    break;
                }
                String vehicleName = fhead.term(0).getText();
                Vehicle vehicle = parsed.vehicleMap().get(vehicleName);
                if (vehicle != null) {
                    ActionCost fuelLeft = ActionCost.valueOf(number);
                    Vehicle newVehicle = vehicle.updateCurFuelCapacity(fuelLeft);
                    parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                    break;
                }
                break;
            }
            case "fuel-max": {
                if (!domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                    logger.debug("Fuel fuel-related function ({}) in non-fuel domain, skipping.",
                            functionSymbol.getText());
                    break;
                }
                String vehicleName = fhead.term(0).getText();
                Vehicle vehicle = parsed.vehicleMap().get(vehicleName);
                if (vehicle != null) {
                    ActionCost fuelMax = ActionCost.valueOf(number);
                    Vehicle newVehicle = vehicle.updateMaxFuelCapacity(fuelMax);
                    parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                    break;
                }
                break;
            }
            case "fuel-demand": {
                if (!domain.getPddlLabels().contains(PddlLabel.Fuel)) {
                    logger.debug("Fuel fuel-related function ({}) in non-fuel domain, skipping.",
                            functionSymbol.getText());
                    break;
                }
                String fromName = fhead.term(0).getText();
                String toName = fhead.term(1).getText();
                Location from = parsed.graph().getLocation(fromName);
                Location to = parsed.graph().getLocation(toName);
                Road road = parsed.graph().getShortestRoadBetween(from, to);
                parsed.graph().putRoad(FuelRoad.build(road, ActionCost.valueOf(number)), from, to);
                break;
            }
            default:
                break; // ignore other functions
        }
    }

    /**
     * Parse the {@code (:objects ...)} context.
     *
     * @param objectDecl the context to parse
     * @param parsed intermediate problem properties aggregator
     */
    private void parseObjectDecl(PddlParser.ObjectDeclContext objectDecl, ParsedProblemContainer parsed) {
        int maxCapacity = -1;
        for (PddlParser.SingleTypeNameListContext typeNameListContext : objectDecl.typedNameList()
                .singleTypeNameList()) {
            String typeName = typeNameListContext.type().getText();
            String objectName = typeNameListContext.NAME(0).getText();
            switch (typeName) {
                case "vehicle":
                    parsed.vehicleMap().put(objectName, new Vehicle(objectName, null, null, null, null, true,
                            Collections.emptyList()));
                    break;
                case "package":
                    parsed.packageMap().put(objectName, new Package(objectName, null, null, ActionCost.ONE));
                    break;
                case "location":
                    parsed.graph().addLocation(new Location(objectName, 0, 0,
                            domain.getPddlLabels().contains(PddlLabel.Fuel) ? false : null));
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

    /**
     * Parse the {@code (:goal ...)} context.
     *
     * @param goalContext the context to parse
     * @param parsed intermediate problem properties aggregator
     */
    private static void parseGoalDescContext(PddlParser.GoalContext goalContext, ParsedProblemContainer parsed) {
        for (PddlParser.GoalDescContext goalDescContext : goalContext.goalDesc().goalDesc()) { // we add 'and' implictly
            String predicate = goalDescContext.atomicTermFormula().predicate().getText();
            String arg1 = goalDescContext.atomicTermFormula().term(0).getText();
            String arg2 = goalDescContext.atomicTermFormula().term(1).getText();
            switch (predicate) {
                case "at": {
                    Location target = parsed.graph().getLocation(arg2);

                    Package pkg = parsed.packageMap().get(arg1);
                    if (pkg != null) {
                        Package newpkg = pkg.updateTarget(target);
                        parsed.packageMap().put(newpkg.getName(), newpkg);
                        break;
                    }

                    Vehicle vehicle = parsed.vehicleMap().get(arg1);
                    if (vehicle != null) {
                        Vehicle newVehicle = vehicle.updateTarget(target);
                        parsed.vehicleMap().put(newVehicle.getName(), newVehicle);
                        break;
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Invalid predicate in goalDesc: " + predicate);
            }
        }
    }

    /**
     * A mutable {@link Problem} builder utility class, used for intermediate data during parsing.
     */
    private static final class ParsedProblemContainer {

        private final String name;
        private final Map<String, Vehicle> vehicleMap = new HashMap<>();
        private final Map<String, Package> packageMap = new HashMap<>();
        private final RoadGraph graph;

        /**
         * Default constructor. Initializes the graph.
         *
         * @param name the name of the problem
         */
        ParsedProblemContainer(String name) {
            this.name = name;
            graph = new DefaultRoadGraph(name + "_graph");
        }

        /**
         * Get the name.
         *
         * @return the name
         */
        public String name() {
            return name;
        }

        /**
         * Get the vehicle map.
         *
         * @return the vehicle map
         */
        public Map<String, Vehicle> vehicleMap() {
            return vehicleMap;
        }

        /**
         * Get the package map.
         *
         * @return the package map
         */
        public Map<String, Package> packageMap() {
            return packageMap;
        }

        /**
         * Get the graph.
         *
         * @return the graph
         */
        public RoadGraph graph() {
            return graph;
        }
    }
}
