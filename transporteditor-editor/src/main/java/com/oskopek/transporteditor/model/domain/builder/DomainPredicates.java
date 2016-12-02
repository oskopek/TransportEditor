package com.oskopek.transporteditor.model.domain.builder;

import com.oskopek.transporteditor.model.domain.action.functions.Function;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;

import java.util.List;
import java.util.Map;

public interface DomainPredicates {

    Map<String, Class<? extends Predicate>> predicateMap();
    Map<String, Class<? extends Function>> functionMap();

    List<Predicate> drivePreconditions();
    List<Predicate> driveEffects();

    List<Predicate> pickUpPreconditions();
    List<Predicate> pickUpEffects();

    List<Predicate> dropPreconditions();
    List<Predicate> dropEffects();
    
    List<Predicate> refuelPreconditions();
    List<Predicate> refuelEffects();

}
