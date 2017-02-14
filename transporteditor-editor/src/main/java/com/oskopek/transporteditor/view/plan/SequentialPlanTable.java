package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javaslang.collection.Stream;
import org.controlsfx.control.table.TableFilter;

import java.util.Collection;
import java.util.List;

public final class SequentialPlanTable {

    private SequentialPlanTable() {
        // intentionally empty
    }

    public static TableFilter<TemporalPlanAction> build(Collection<TemporalPlanAction> actions) {
        List<TemporalPlanAction> actionList = Stream.ofAll(actions)
                .sortBy(TemporalPlanAction::getStartTimestamp).toJavaList();
        TableView<TemporalPlanAction> tableView = new TableView<>(FXCollections.observableList(actionList));

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

        tableView.getColumns().setAll(actionColumn, whoColumn, whereColumn, whatColumn);
        tableView.getColumns().forEach(c -> c.setSortable(false));
        return TableFilter.forTableView(tableView).lazy(true).apply();
    }

}
