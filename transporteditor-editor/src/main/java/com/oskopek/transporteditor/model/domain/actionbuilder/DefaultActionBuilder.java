/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.DefaultAction;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;

import java.util.List;

public abstract class DefaultActionBuilder<Who extends DefaultAction<Where, What>, Where extends Locatable, What
        extends ActionObject>
        implements ActionBuilder<Who, Where, What> {

    private final List<Predicate> preconditions;
    private final List<Predicate> effects;

    public DefaultActionBuilder(List<Predicate> preconditions, List<Predicate> effects) {
        this.preconditions = preconditions;
        this.effects = effects;
    }

    @Override
    public List<Predicate> getPreconditions() {
        return preconditions;
    }

    @Override
    public List<Predicate> getEffects() {
        return effects;
    }
}
