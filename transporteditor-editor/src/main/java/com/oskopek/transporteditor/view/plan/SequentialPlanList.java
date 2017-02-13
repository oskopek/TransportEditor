package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.Plan;
import javafx.beans.property.ReadOnlyIntegerWrapper;
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

    public static TableView<TemporalPlanAction> build(Plan plan) {
        List<TemporalPlanAction> actionList = Stream.ofAll(plan.getTemporalPlanActions())
                .sortBy(TemporalPlanAction::getEndTimestamp).sortBy(TemporalPlanAction::getStartTimestamp).toJavaList();
        TableView<TemporalPlanAction> tableView = new TableView<>(FXCollections.observableList(actionList));

        TableColumn<TemporalPlanAction, Number> startColumn = new TableColumn<>("Start");
        startColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyIntegerWrapper(param.getValue().getStartTimestamp()));
        TableColumn<TemporalPlanAction, Number> endColumn = new TableColumn<>("End");
        endColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyIntegerWrapper(param.getValue().getEndTimestamp()));
        TableColumn<TemporalPlanAction, String> actionColumn = new TableColumn<>("Action");
        actionColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getAction().getName()));
        TableColumn<TemporalPlanAction, String> whoColumn = new TableColumn<>("Who");
        whoColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getAction().getWho().getName()));
        TableColumn<TemporalPlanAction, String> whereColumn = new TableColumn<>("Where");
        whereColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyStringWrapper(param.getValue().getAction().getWhere().getName()));
        TableColumn<TemporalPlanAction, String> whatColumn = new TableColumn<>("What");
        whatColumn.cellValueFactoryProperty().setValue(param -> new ReadOnlyStringWrapper(
                param.getValue().getAction().getWhat() == null ? ""
                        : param.getValue().getAction().getWhat().getName()));

        tableView.getColumns().setAll(startColumn, endColumn, actionColumn, whoColumn, whereColumn, whatColumn);
        return tableView;
    }

}
