package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javaslang.collection.Stream;

import java.util.List;

public final class SequentialPlanList {

    private SequentialPlanList() {
        // intentionally empty
    }

    public static TableView<Action> build(Plan plan) {
        List<Action> actionList = Stream.ofAll(plan.getTemporalPlanActions()).sortBy(
                TemporalPlanAction::getEndTimestamp).sortBy(TemporalPlanAction::getStartTimestamp).map(
                TemporalPlanAction::getAction).toJavaList();
        TableView<Action> tableView = new TableView<>(FXCollections.observableList(actionList));

        TableColumn<Action, String> actionColumn = new TableColumn<>("Action");
        actionColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getName()));
        TableColumn<Action, String> whoColumn = new TableColumn<>("Who");
        whoColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getWho().getName()));
        TableColumn<Action, String> whereColumn = new TableColumn<>("Where");
        whereColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getWhere().getName()));
        TableColumn<Action, String> whatColumn = new TableColumn<>("What");
        whatColumn.cellValueFactoryProperty().setValue(param -> new ReadOnlyStringWrapper(
                param.getValue().getWhat() == null ? "" : param.getValue().getWhat().getName()));

        tableView.getColumns().setAll(actionColumn, whoColumn, whereColumn, whatColumn);
        return tableView;
    }

}
