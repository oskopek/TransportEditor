package com.oskopek.transporteditor.model.planner;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.view.executables.LogStreamable;
import javafx.beans.value.ObservableValue;

import java.util.concurrent.CompletionStage;

public interface Planner extends LogStreamable {

    CompletionStage<Plan> startAsync(Domain domain, Problem problem);

    Plan startAndWait(Domain domain, Problem problem);

    Plan getBestPlan();

    ObservableValue<Plan> getCurrentPlan();

    ObservableValue<Boolean> isPlanning();

}
