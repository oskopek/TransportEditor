package com.oskopek.transporteditor.model.domain.builder;

import com.oskopek.transporteditor.model.domain.action.functions.*;
import com.oskopek.transporteditor.model.domain.action.predicates.*;
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
        public List<Predicate> drivePreconditions() {
            return Arrays.asList();
        }

        @Override
        public List<Predicate> driveEffects() {
            return Arrays.asList();
        }

        @Override
        public List<Predicate> pickUpPreconditions() {
            return Collections.emptyList();
        }

        @Override
        public List<Predicate> pickUpEffects() {
            return Collections.emptyList();
        }

        @Override
        public List<Predicate> dropPreconditions() {
            return Collections.emptyList();
        }

        @Override
        public List<Predicate> dropEffects() {
            return Collections.emptyList();
        }

        @Override
        public List<Predicate> refuelPreconditions() {
            return Arrays.asList();
        }

        @Override
        public List<Predicate> refuelEffects() {
            return Arrays.asList();
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
        private final List<Predicate> drivePreconditions;
        private final List<Predicate> driveEffects;
        private final List<Predicate> pickUpPreconditions;
        private final List<Predicate> pickUpEffects;
        private final List<Predicate> dropPreconditions;
        private final List<Predicate> dropEffects;
        private final List<Predicate> refuelPreconditions;
        private final List<Predicate> refuelEffects;

        public VariableDomainPredicates(Map<String, Class<? extends Function>> functionMap,
                Map<String, Class<? extends Predicate>> predicateMap,
                List<Predicate> drivePreconditions,
                List<Predicate> driveEffects,
                List<Predicate> pickUpPreconditions,
                List<Predicate> pickUpEffects,
                List<Predicate> dropPreconditions,
                List<Predicate> dropEffects,
                List<Predicate> refuelPreconditions,
                List<Predicate> refuelEffects) {
            this.functionMap = functionMap;
            this.predicateMap = predicateMap;
            this.drivePreconditions = drivePreconditions;
            this.driveEffects = driveEffects;
            this.pickUpPreconditions = pickUpPreconditions;
            this.pickUpEffects = pickUpEffects;
            this.dropPreconditions = dropPreconditions;
            this.dropEffects = dropEffects;
            this.refuelPreconditions = refuelPreconditions;
            this.refuelEffects = refuelEffects;
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
            return drivePreconditions;
        }

        @Override
        public List<Predicate> driveEffects() {
            return driveEffects;
        }

        @Override
        public List<Predicate> pickUpPreconditions() {
            return pickUpPreconditions;
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
