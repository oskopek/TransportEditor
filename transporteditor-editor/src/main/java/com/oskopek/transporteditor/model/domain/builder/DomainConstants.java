package com.oskopek.transporteditor.model.domain.builder;

import com.oskopek.transporteditor.model.domain.action.functions.*;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
import javaslang.Tuple;
import javaslang.Tuple2;
import scala.collection.mutable.*;

import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DomainConstants {



    public static DomainPredicates toTemporal(DomainPredicates domainPredicates) {
        // TODO
    }

    public static DomainPredicates aggregate(DomainPredicates... domainPredicates) {
        return new VariableDomainPredicates(
                Arrays.stream(domainPredicates).map(DomainPredicates::functionMap).collect(HashMap::new, Map::putAll, Map::putAll),
                Arrays.stream(domainPredicates).map(DomainPredicates::predicateMap).collect(HashMap::new, Map::putAll, Map::putAll),
                Arrays.stream(domainPredicates).map(DomainPredicates::drivePreconditions))
    }

    public static final class Fuel implements DomainPredicates {

        @Override
        public Map<String, Class<? extends Predicate>> predicateMap() {
            Map<String, Class<? extends Predicate>> predicateMap = new HashMap<>(4);
            predicateMap.put("has-petrol-station", HasPetrolStation.class);
            return predicateMap;
        }

        @Override
        public Map<String, Class<? extends Function>> functionMap() {
            Map<String, Class<? extends Function>> functionMap = new HashMap<>(4);
            functionMap.put("fuel-left", FuelLeft.class);
            functionMap.put("fuel-max", FuelMax.class);
            functionMap.put("fuel-demand", FuelDemand.class);
            return functionMap;
        }

        @Override
        public Map<String, Tuple2<List<Predicate>, List<Predicate>>> actionPredicatesEffectsMap() {
            Map<String, Class<? extends Function>> functionMap = new HashMap<>(4);
        }
    }

    public static final class Sequential implements DomainPredicates {

        @Override
        public Map<String, Class<? extends Predicate>> predicateMap() {
            Map<String, Class<? extends Predicate>> predicateMap = new HashMap<>(4);
            predicateMap.put("at", WhoAtWhere.class);
            predicateMap.put("capacity", HasCapacity.class);
            predicateMap.put("in", In.class);
            predicateMap.put("road", IsRoad.class);
            return predicateMap;
        }

        @Override
        public Map<String, Class<? extends Function>> functionMap() {
            Map<String, Class<? extends Function>> functionMap = new HashMap<>(2);
            functionMap.put("road-length", RoadLength.class);
            functionMap.put("total-cost", TotalCost.class);
            return functionMap;
        }

        @Override
        public List<Predicate> drivePreconditions() {
            return Arrays.asList(new WhoAtWhere(), new IsRoad());
        }

        @Override
        public List<Predicate> driveEffects() {
            return Arrays.asList(new Not(new WhoAtWhere()), new WhoAtWhat());
        }

        @Override
        public List<Predicate> pickUpPreconditions() {
            return null; // TODO: fail
        }

        @Override
        public List<Predicate> pickUpEffects() {
            return null;
        }

        @Override
        public List<Predicate> dropPreconditions() {
            return Arrays.asList(new WhoAtWhere(), new In());
        }

        @Override
        public List<Predicate> dropEffects() {
            return Arrays.asList(new Not(new In()), new WhatAtWhere());
        }

        @Override
        public List<Predicate> refuelPreconditions() {
            return Arrays.asList(new WhoAtWhere(), new WhatAtWhere());
        }

        @Override
        public List<Predicate> refuelEffects() {
            return Arrays.asList(new Not(new WhatAtWhere()), new In());
        }
    }

    private static final class VariableDomainPredicates implements DomainPredicates {
        private final Map<String, Class<? extends Function>> functionMap;
        private final Map<String, Class<? extends Predicate>> predicateMap;
        private final Map<String, Tuple2<List<Predicate>, List<Predicate>>> actionPredicateEffects;

        public VariableDomainPredicates(Map<String, Class<? extends Function>> functionMap,
                Map<String, Class<? extends Predicate>> predicateMap,
                Map<String, Tuple2<List<Predicate>, List<Predicate>>> actionPredicateEffects) {
            this.functionMap = functionMap;
            this.predicateMap = predicateMap;
            this.actionPredicateEffects = actionPredicateEffects;
        }

        @Override
        public Map<String, Class<? extends Predicate>> predicateMap() {
            return predicateMap;
        }

        @Override
        public Map<String, Class<? extends Function>> functionMap() {
            return functionMap;
        }

        @Override
        public List<Predicate> drivePreconditions() {
            return actionPredicateEffects.get("drive")._1;
        }

        @Override
        public List<Predicate> driveEffects() {
            return actionPredicateEffects.get("drive")._2;
        }

        @Override
        public List<Predicate> pickUpPreconditions() {
            return actionPredicateEffects.get("pick-up")._1;
        }

        @Override
        public List<Predicate> pickUpEffects() {
            return pickUpEffects;
        }

        @Override
        public List<Predicate> dropPreconditions() {
            return dropPreconditions;
        }

        @Override
        public List<Predicate> dropEffects() {
            return dropEffects;
        }

        @Override
        public List<Predicate> refuelPreconditions() {
            return refuelPreconditions;
        }

        @Override
        public List<Predicate> refuelEffects() {
            return refuelEffects;
        }
    }


}
