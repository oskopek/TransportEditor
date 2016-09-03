/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.DomainType;
import com.oskopek.transporteditor.planning.domain.VariableDomain;
import com.oskopek.transporteditor.planning.domain.action.functions.*;
import com.oskopek.transporteditor.planning.domain.action.predicates.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

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

    private Stream<String> normalizeInput(String contents) {
        String[] lines = contents.split("\n");
        return Arrays.stream(lines).map(String::trim).map(s -> s.replaceFirst(";.*", "")).filter(s -> !s.isEmpty());
    }

    private List<Class<? extends Predicate>> parsePredicates(String contents) {
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

        return predicates.stream().map(predicateNameMap::get).filter(aClass -> aClass != null).collect(
                Collectors.toList());
    }

    private List<Class<? extends Function>> parseFunctions(String contents) {
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

        return functions.stream().map(functionNameMap::get).filter(aClass -> aClass != null).collect(
                Collectors.toList());
    }

    private DomainType parseDomainType(String contents) {
        if (contents.contains(":action-costs")) {
            return DomainType.ActionCost;
        } else if (contents.contains(":goal-utilities")) {
            return DomainType.Numeric;
        } else if (contents.contains(":durative-actions")) {
            return DomainType.Temporal;
        } else {
            return null;
        }
    }

    @Override
    public VariableDomain parse(String contents) throws IllegalArgumentException {
        List<Class<? extends Predicate>> predicates = parsePredicates(contents);
        List<Class<? extends Function>> functions = parseFunctions(contents);
        DomainType type = parseDomainType(contents);
        return new VariableDomain(type, predicates, functions);
    }

    @Override
    public String serialize(VariableDomain object) throws IllegalArgumentException {
        Map<String, Object> input = new HashMap<>();
        input.put("date", new Date());
        input.put("domain", object);
        input.put("actionCost", DomainType.ActionCost);
        input.put("numeric", DomainType.Numeric);
        input.put("temporal", DomainType.Temporal);

        input.put("Capacity", Capacity.class);
        input.put("FuelDemand", FuelDemand.class);
        input.put("FuelLeft", FuelLeft.class);
        input.put("FuelMax", FuelMax.class);
        input.put("PackageSize", PackageSize.class);
        input.put("RoadLength", RoadLength.class);
        input.put("TotalCost", TotalCost.class);

        input.put("At", At.class);
        input.put("HasCapacity", HasCapacity.class);
        input.put("HasPetrolStation", HasPetrolStation.class);
        input.put("In", In.class);
        input.put("IsRoad", IsRoad.class);
        input.put("ReadyLoading", ReadyLoading.class);

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
