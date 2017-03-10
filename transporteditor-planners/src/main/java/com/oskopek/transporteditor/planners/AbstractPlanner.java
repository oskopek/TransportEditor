package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.AbstractLogStreamable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

/**
 * A planner implementation for simple Java-based planners. Handles listeners
 * and all needed JavaFX properties. Extensions only need to handle:
 * <ul>
 *     <li>planning: {@link #plan(Domain, Problem)}</li>
 *     <li>logging, using {@link #log(String)}</li>
 * </ul>
 */
public abstract class AbstractPlanner extends AbstractLogStreamable implements Planner {

    private final ObjectProperty<Plan> planProperty = new SimpleObjectProperty<>();
    private final BooleanProperty isPlanningProperty = new SimpleBooleanProperty(false);

    /**
     * Default empty constructor as per {@link Planner} requirements.
     */
    public AbstractPlanner() {
        // intentionally empty
    }

    /**
     * Create a plan for the given problem and domain. Will return empty {@link Optional} if no plan could be found,
     * for any reason.
     *
     * @param domain the domain
     * @param problem the problem
     * @return the plan, or nothing
     */
    public abstract Optional<Plan> plan(Domain domain, Problem problem);

    @Override
    public final Plan startAndWait(Domain domain, Problem problem) {
        isPlanningProperty.setValue(true);
        Plan plan = plan(domain, problem).orElse(null);
        isPlanningProperty.setValue(false);
        planProperty.setValue(plan);
        return plan;
    }

    @Override
    public final ObservableValue<Plan> currentPlanProperty() {
        return planProperty;
    }

    @Override
    public final ObservableValue<Boolean> isPlanning() {
        return isPlanningProperty;
    }

    @Override
    public final boolean isAvailable() {
        return true;
    }
}
