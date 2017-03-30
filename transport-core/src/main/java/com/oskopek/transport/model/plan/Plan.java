package com.oskopek.transport.model.plan;

import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Interface representing plans for Transport domain problems.
 */
public interface Plan extends Iterable<TemporalPlanAction> {

    /**
     * Get a collection of all actions contained in this plan. Does not have a specific order by default,
     * implementations may choose to override this.
     *
     * @return the actions
     */
    Collection<Action> getActions();

    /**
     * Get a collection of all temporal actions contained in this plan. Does not have a specific order by default,
     * implementations may choose to override this.
     *
     * @return the temporal actions
     */
    Collection<TemporalPlanAction> getTemporalPlanActions();

    /**
     * {@inheritDoc}
     * <p>
     * Does not iterate over actions in any specific order.
     */
    @Override
    default Iterator<TemporalPlanAction> iterator() {
        return getTemporalPlanActions().iterator();
    }

    @Override
    default void forEach(Consumer<? super TemporalPlanAction> action) {
        getTemporalPlanActions().forEach(action);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Does not iterate over actions in any specific order.
     */
    @Override
    default Spliterator<TemporalPlanAction> spliterator() {
        return getTemporalPlanActions().spliterator();
    }
}
