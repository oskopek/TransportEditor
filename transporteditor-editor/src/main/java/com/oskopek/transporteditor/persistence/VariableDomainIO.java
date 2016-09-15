/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.DomainLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.domain.action.functions.*;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import com.oskopek.transporteditor.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.PickUpBuilder;
import com.oskopek.transporteditor.model.domain.actionbuilder.RefuelBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.lanwen.verbalregex.VerbalExpression;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariableDomainIO implements DataReader<VariableDomain>, DataWriter<VariableDomain> {

    private static final Map<String, Class<? extends Predicate>> predicateNameMap = new HashMap<>();
    private static final Map<String, Class<? extends Function>> functionNameMap = new HashMap<>();

    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);

    public static String parseName(String contents) {
        return contents.split("\n")[0].replaceAll(";", "").trim();
    }

    private Stream<String> normalizeInput(String contents) {
        String[] lines = contents.split("\n");
        return Arrays.stream(lines).map(String::trim).map(s -> s.replaceFirst(";.*", "")).filter(s -> !s.isEmpty());
    }

    private Map<String, Class<? extends Predicate>> parsePredicates(String contents) {
        List<String> predicates = new ArrayList<>();
        String normalized = normalizeInput(contents).map(s -> s.replaceAll("\\s+", "")).collect(Collectors.joining(""));

        String individualPredicateRegex = "\\(([-a-zA-Z]+)[^)]*\\)";
        Pattern predicatePattern = Pattern.compile("\\(:predicates(" + individualPredicateRegex + ")*\\)");
        Pattern individualPredicatePattern = Pattern.compile(individualPredicateRegex);

        Matcher predGrpMatcher = predicatePattern.matcher(normalized);
        if (predGrpMatcher.find()) {
            String predicateGrp = predGrpMatcher.group(0);
            Matcher predMatcher = individualPredicatePattern.matcher(predicateGrp);
            while (predMatcher.find()) {
                predicates.add(predMatcher.group(1));
            }
        }

        return predicates.stream().filter(p -> !"capacity-predecessor".equals(p)).collect(
                Collectors.toMap(p -> p, predicateNameMap::get));
    }

    private Map<String, Class<? extends Function>> parseFunctions(String contents) {
        List<String> functions = new ArrayList<>();

        String normalized = normalizeInput(contents).map(s -> s.replaceAll("\\s+", "")).collect(Collectors.joining(""));

        String individualFunctionRegex = "\\(([-a-zA-Z]+)[^)]*\\)(-[-a-zA-Z]+)?";
        Pattern functionPattern = Pattern.compile("\\(:functions(" + individualFunctionRegex + ")*\\)");
        Pattern individualFunctionPattern = Pattern.compile(individualFunctionRegex);

        Matcher funcGrpMatcher = functionPattern.matcher(normalized);
        if (funcGrpMatcher.find()) {
            String funcGrp = funcGrpMatcher.group(0);
            Matcher funcMatcher = individualFunctionPattern.matcher(funcGrp);
            while (funcMatcher.find()) {
                functions.add(funcMatcher.group(1));
            }
        }

        return functions.stream().collect(Collectors.toMap(f -> f, functionNameMap::get));
    }

    private Set<DomainLabel> parseDomainLabels(String contents) {
        Set<DomainLabel> domainLabels = new HashSet<>();
        if (contents.contains(":action-costs")) {
            domainLabels.add(DomainLabel.ActionCost);
        }
        if (contents.contains(":goal-utilities")) {
            domainLabels.add(DomainLabel.Numeric);
        }
        if (contents.contains(":durative-actions")) {
            domainLabels.add(DomainLabel.Temporal);
        }
        return domainLabels;
    }

    public DriveBuilder parseDriveBuilder(String contents) {
        return new DriveBuilder(null, null);
    }

    public DropBuilder parseDropBuilder(String contents) {
        return new DropBuilder(null, null, null, null);
    }

    public PickUpBuilder parsePickUpBuilder(String contents) {
        Map<String, String> parsed = actionParser("pick-up", contents);
        return new PickUpBuilder(new ArrayList<>(), null, null, null);
    }

    public RefuelBuilder parseRefuelBuilder(String contents) {
        Map<String, String> parsed = actionParser("refuel", contents);
        return new RefuelBuilder(null, null, null, null);
    }

    private Map<String, String> actionParser(String name, String normalized) {
        VerbalExpression.Builder simpleName = VerbalExpression.regex().capt().add("[-:?a-zA-Z0-9]").oneOrMore()
                .endCapt();
        VerbalExpression.Builder simplePredicate = VerbalExpression.regex().space().zeroOrMore().then("(").capt().add(
                simpleName).space().oneOrMore().endCapt().zeroOrMore().add(simpleName).then(")").space().zeroOrMore();
        VerbalExpression.Builder complexPredicate = VerbalExpression.regex().space().zeroOrMore().then("(").add(
                simpleName).space().oneOrMore().add(simplePredicate).zeroOrMore().then(")").space().zeroOrMore();
        VerbalExpression actionExpression = VerbalExpression.regex().then("(:").maybe("durative-").then("action")
                .space().oneOrMore().then(name).space().oneOrMore().then(":parameters").space().zeroOrMore().add(
                        simplePredicate).capt().then(":duration").space().zeroOrMore().add(simplePredicate).endCapt()
                .count(0, 1).then(":").maybe("pre").then("condition").space().oneOrMore().add(complexPredicate).then(
                        ":effect").space().oneOrMore().add(complexPredicate).space().zeroOrMore().then(")").build();

        System.out.println(actionExpression.getText(normalized));
        Map<String, String> parsed = new HashMap<>();
        return parsed;
    }

    @Override
    public VariableDomain parse(String contents) throws IllegalArgumentException {
        String name = parseName(contents);
        Map<String, Class<? extends Predicate>> predicates = parsePredicates(contents);
        Map<String, Class<? extends Function>> functions = parseFunctions(contents);
        String normalized = normalizeInput(contents).collect(Collectors.joining(""));
        DriveBuilder driveBuilder = parseDriveBuilder(normalized);
        DropBuilder dropBuilder = parseDropBuilder(normalized);
        PickUpBuilder pickUpBuilder = parsePickUpBuilder(normalized);
        RefuelBuilder refuelBuilder = parseRefuelBuilder(normalized);

        Set<DomainLabel> labels = parseDomainLabels(contents);
        if (functions.containsKey("capacity")) {
            labels.add(DomainLabel.Capacity);
            if (pickUpBuilder.getPreconditions().stream().map(Object::getClass).anyMatch(HasCapacity.class::equals)) {
                labels.add(DomainLabel.MaxCapacity); // TODO: probably doesn't work correctly for nested predicates
            }
        } else if (predicates.containsKey("capacity")) {
            labels.add(DomainLabel.MaxCapacity);
            labels.add(DomainLabel.Capacity);
        }
        return new VariableDomain(name, driveBuilder, dropBuilder, pickUpBuilder,
                refuelBuilder, labels, predicates, functions);
    }

    @Override
    public String serialize(VariableDomain object) throws IllegalArgumentException {
        Map<String, Object> input = new HashMap<>();
        input.put("date", new Date());
        input.put("domain", object);
        input.put("actionCost", DomainLabel.ActionCost);
        input.put("numeric", DomainLabel.Numeric);
        input.put("temporal", DomainLabel.Temporal);

        //        input.put("Capacity", Capacity.class);
        //        input.put("FuelDemand", FuelDemand.class);
        //        input.put("FuelLeft", FuelLeft.class);
        //        input.put("FuelMax", FuelMax.class);
        //        input.put("PackageSize", PackageSize.class);
        //        input.put("RoadLength", RoadLength.class);
        //        input.put("TotalCost", TotalCost.class);
        //
        //        input.put("At", At.class);
        //        input.put("HasCapacity", HasCapacity.class);
        //        input.put("HasPetrolStation", HasPetrolStation.class);
        //        input.put("In", In.class);
        //        input.put("IsRoad", IsRoad.class);
        //        input.put("ReadyLoading", ReadyLoading.class);

        Template template = null;
        try {
            template = configuration.getTemplate("domain.pddl.ftl");
        } catch (IOException e) {
            throw new IllegalStateException("Error occurred during reading template file.", e);
        }

        StringWriter writer = new StringWriter();
        try {
            template.process(input, writer);
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Error occurred during processing template.", e);
        }
        return writer.toString();
    }

    static {
        predicateNameMap.put("at", At.class);
        predicateNameMap.put("capacity", HasCapacity.class);
        predicateNameMap.put("has-petrol-station", HasPetrolStation.class);
        predicateNameMap.put("in", In.class);
        predicateNameMap.put("road", IsRoad.class);
        predicateNameMap.put("ready-loading", ReadyLoading.class);
    }

    static {
        functionNameMap.put("capacity", Capacity.class);
        functionNameMap.put("fuel-demand", FuelDemand.class);
        functionNameMap.put("fuel-left", FuelLeft.class);
        functionNameMap.put("fuel-max", FuelMax.class);
        functionNameMap.put("package-size", PackageSize.class);
        functionNameMap.put("road-length", RoadLength.class);
        functionNameMap.put("total-cost", TotalCost.class);
    }

    static {
        configuration.setClassForTemplateLoading(VariableDomainIO.class, "");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }
}
