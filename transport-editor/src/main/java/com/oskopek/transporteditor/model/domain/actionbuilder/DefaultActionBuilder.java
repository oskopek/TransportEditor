package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.DefaultAction;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Default action builder abstract implementation handling preconditions and effects.
 *
 * @param <Who> the who type
 * @param <Where> the where type
 * @param <What> the what type
 */
public abstract class DefaultActionBuilder<Who extends DefaultAction<Where, What>, Where extends Locatable, What
        extends ActionObject>
        implements ActionBuilder<Who, Where, What> {

    private final List<Predicate> preconditions;
    private final List<Predicate> effects;

    /**
     * Default constructor.
     *
     * @param preconditions the preconditions
     * @param effects the effects
     */
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getPreconditions())
                .append(getEffects())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultActionBuilder)) {
            return false;
        }
        DefaultActionBuilder<?, ?, ?> that = (DefaultActionBuilder<?, ?, ?>) o;
        return new EqualsBuilder()
                .append(getPreconditions(), that.getPreconditions())
                .append(getEffects(), that.getEffects())
                .isEquals();
    }
}
