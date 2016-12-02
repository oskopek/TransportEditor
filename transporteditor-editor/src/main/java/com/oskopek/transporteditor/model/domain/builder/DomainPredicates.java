package com.oskopek.transporteditor.model.domain.builder;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import javaslang.Tuple2;

import java.util.List;
import java.util.Map;

public interface DomainPredicates {

    Map<String, Class<? extends Predicate>> predicateMap();
    Map<String, Class<? extends Function>> functionMap();

    Map<String, Tuple2<List<Predicate>, List<Predicate>>> actionPredicatesEffectsMap();

}
