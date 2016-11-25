package com.oskopek.transporteditor.model.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public interface Plan extends Iterable<TemporalPlanAction> {

    Collection<Action> getActions();

    Collection<TemporalPlanAction> getTemporalPlanActions();

    default TemporalPlan toTemporalPlan() {
        return new TemporalPlan(getTemporalPlanActions());
    }

    @Override
    default Iterator<TemporalPlanAction> iterator() {
        return getTemporalPlanActions().iterator();
    }

    @Override
    default void forEach(Consumer<? super TemporalPlanAction> action) {
        getTemporalPlanActions().forEach(action);
    }

    @Override
    default Spliterator<TemporalPlanAction> spliterator() {
        return getTemporalPlanActions().spliterator();
    }
}
