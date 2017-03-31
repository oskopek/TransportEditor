package com.oskopek.transport.view.plan;

import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.view.NumberDoubleStringConverter;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javaslang.collection.Stream;
import org.controlsfx.control.table.TableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Filterable {@link TableView} of {@link TemporalPlanAction}s with editable start times, used for visualizing
 * {@link com.oskopek.transport.model.plan.TemporalPlan}s.
 */
public final class TemporalPlanTable {

    private static final transient Logger logger = LoggerFactory.getLogger(TemporalPlanTable.class);

    /**
     * Empty constructor.
     */
    private TemporalPlanTable() {
        // intentionally empty
    }

    /**
     * Defines the sorting order of the tables actions.
     *
     * @param actionList the action list to sort.
     * @return an observable sorted list transformed from the input
     */
    private static ObservableList<TemporalPlanAction> sort(List<TemporalPlanAction> actionList) {
        return new ObservableListWrapper<>(Stream.ofAll(actionList).sortBy(TemporalPlanAction::getEndTimestamp)
                .sortBy(TemporalPlanAction::getStartTimestamp).toJavaList());
    }

    /**
     * Build the filtered and partially editable table. Calls the updatePlan callback when a new plan is created and
     * doesn't expect to be disposed afterwards, reorders itself after changes to start times. Uses internal validation
     * for correct time inputs.
     *
     * @param actions the temporal actions to display
     * @param updatePlan the plan update callback
     * @return the {@link TableFilter} controlsfx widget with a backing table
     */
    public static TableFilter<TemporalPlanAction> build(Collection<TemporalPlanAction> actions,
            Consumer<Collection<TemporalPlanAction>> updatePlan) {
        List<TemporalPlanAction> actionList = new ArrayList<>(actions);
        ListProperty<TemporalPlanAction> displayActionList = new SimpleListProperty<>();
        displayActionList.setValue(sort(actionList));
        TableView<TemporalPlanAction> tableView = new TableView<>(displayActionList);

        TableColumn<TemporalPlanAction, Double> startColumn = new TableColumn<>("Start");
        startColumn.cellValueFactoryProperty().setValue(param -> {
            SimpleDoubleProperty startTimeProperty = new SimpleDoubleProperty(param.getValue().getStartTimestamp());
            return startTimeProperty.asObject();
        });
        startColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberDoubleStringConverter()));
        startColumn.editableProperty().setValue(true);
        startColumn.setOnEditCommit(event -> {
            Double newValue = event.getNewValue();
            if (newValue == null) {
                newValue = event.getOldValue();
            }
            TemporalPlanAction oldAction = event.getRowValue();
            TemporalPlanAction newAction = event.getRowValue().updateStartTimestampSmart(newValue);
            actionList.remove(oldAction);
            actionList.add(newAction);
            updatePlan.accept(actionList);
            displayActionList.setValue(sort(actionList));
        });
        TableColumn<TemporalPlanAction, Number> endColumn = new TableColumn<>("End");
        endColumn.cellValueFactoryProperty().setValue(
                param -> new ReadOnlyDoubleWrapper(param.getValue().getEndTimestamp()));
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
