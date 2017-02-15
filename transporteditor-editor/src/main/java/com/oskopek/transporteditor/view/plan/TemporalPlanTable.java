package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javaslang.collection.Stream;
import org.controlsfx.control.table.TableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class TemporalPlanTable {

    private static final transient Logger logger = LoggerFactory.getLogger(TemporalPlanTable.class);

    private TemporalPlanTable() {
        // intentionally empty
    }

    private static ObservableList<TemporalPlanAction> sort(List<TemporalPlanAction> actionList) {
        return new ObservableListWrapper<>(Stream.ofAll(actionList).sortBy(TemporalPlanAction::getEndTimestamp)
                .sortBy(TemporalPlanAction::getStartTimestamp).toJavaList());
    }

    public static TableFilter<TemporalPlanAction> build(Collection<TemporalPlanAction> actions,
            Consumer<Collection<TemporalPlanAction>> updatePlan) {
        List<TemporalPlanAction> actionList = new ArrayList<>(actions);
        ListProperty<TemporalPlanAction> displayActionList = new SimpleListProperty<>();
        displayActionList.setValue(sort(actionList));
        TableView<TemporalPlanAction> tableView = new TableView<>(displayActionList);

        TableColumn<TemporalPlanAction, Integer> startColumn = new TableColumn<>("Start");
        startColumn.cellValueFactoryProperty().setValue(param -> {
            SimpleIntegerProperty startTimeProperty = new SimpleIntegerProperty(param.getValue().getStartTimestamp());
            return startTimeProperty.asObject();
        });
        startColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        startColumn.editableProperty().setValue(true);
        startColumn.setOnEditCommit(event -> {
            TemporalPlanAction oldAction = event.getRowValue();
            TemporalPlanAction newAction = event.getRowValue().updateStartTimestampSmart(event.getNewValue());
            actionList.remove(oldAction);
            actionList.add(newAction);
            updatePlan.accept(actionList);
            displayActionList.setValue(sort(actionList));
        });
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
        tableView.getColumns().forEach(c -> c.setSortable(false));
        tableView.editableProperty().setValue(true);
        return TableFilter.forTableView(tableView).lazy(true).apply();
    }

}
