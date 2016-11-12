package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.DefaultAction;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;

import java.util.List;

public interface ActionBuilder<Action_ extends DefaultAction<Who__, What__>, Who__ extends Locatable, What__ extends
        ActionObject> {

    <Who_ extends Who__, What_ extends What__> Action_ build(Who_ who, Location where, What_ what);

    List<Predicate> getPreconditions();

    List<Predicate> getEffects();

}
