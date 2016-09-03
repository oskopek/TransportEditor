/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.VariableDomain;
import com.oskopek.transporteditor.planning.domain.action.functions.*;
import com.oskopek.transporteditor.planning.domain.action.predicates.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariableDomainGuesser implements DataReader<VariableDomain>, DataWriter<VariableDomain> {

    private final static Map<String, Class<? extends Predicate>> predicateNameMap = new HashMap<>();
    private final static Map<String, Class<? extends Function>> functionNameMap = new HashMap<>();

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

    @Override
    public VariableDomain parse(String contents) throws IllegalArgumentException {
        List<Class<? extends Predicate>> predicates = parsePredicates(contents);
        List<Class<? extends Function>> functions = parseFunctions(contents);
        return new VariableDomain(predicates, functions);
    }

    @Override
    public String serialize(VariableDomain object) throws IllegalArgumentException {
        StringBuilder serialized = new StringBuilder();

        return serialized.toString();
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
}
