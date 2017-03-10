package com.oskopek.transporteditor.planner;

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

public abstract class AbstractPlanner extends AbstractLogStreamable implements Planner {

    private final ObjectProperty<Plan> planProperty = new SimpleObjectProperty<>();
    private final BooleanProperty isPlanningProperty = new SimpleBooleanProperty(false);

    public AbstractPlanner() {
        // intentionally empty
    }

    public abstract Plan plan(Domain domain, Problem problem);

    @Override
    public final Plan startAndWait(Domain domain, Problem problem) {
        isPlanningProperty.setValue(true);
        Plan plan = plan(domain, problem);
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
