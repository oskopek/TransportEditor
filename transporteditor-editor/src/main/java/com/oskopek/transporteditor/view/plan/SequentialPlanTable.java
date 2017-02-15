package com.oskopek.transporteditor.view.plan;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.domain.action.TemporalPlanAction;
import com.oskopek.transporteditor.model.plan.SequentialPlan;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.*;
import javafx.util.Callback;
import org.controlsfx.control.table.TableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SequentialPlanTable {

    private static final DataFormat serializedMimeType = new DataFormat("application/x-java-serialized-object");
    private static final transient Logger logger = LoggerFactory.getLogger(SequentialPlanTable.class);

    private static final Function<TableRow<?>, EventHandler<? super MouseEvent>> tableDragDetected = row -> event -> {
        if (!row.isEmpty()) {
            Integer index = row.getIndex();
            logger.trace("Dragging row {}", index);
            Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
            dragboard.setDragView(row.snapshot(null, null));
            ClipboardContent clipboard = new ClipboardContent();
            clipboard.put(serializedMimeType, index);
            dragboard.setContent(clipboard);
            event.consume();
        }
    };

    private static final Function<TableRow<?>, EventHandler<? super DragEvent>> tableDragOver = row -> event -> {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasContent(serializedMimeType)) {
            if (row.getIndex() != (Integer) dragboard.getContent(serializedMimeType)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        }
    };

    private SequentialPlanTable() {
        // intentionally empty
    }

    public static TableFilter<TemporalPlanAction> build(Collection<Action> actions,
            BiConsumer<List<Action>, Integer> updatePlan) {
        List<Action> actionList = new ArrayList<>(actions);
        List<TemporalPlanAction> temporalPlanActions
                = new ArrayList<>(new SequentialPlan(actionList).getTemporalPlanActionsList());
        TableView<TemporalPlanAction> tableView = new TableView<>(FXCollections.observableList(temporalPlanActions));

        TableColumn<TemporalPlanAction, String> dragColumn = new TableColumn<>();
        dragColumn.cellValueFactoryProperty().setValue(param -> new ReadOnlyStringWrapper("☰"));
        Callback<TableColumn<TemporalPlanAction, String>, TableCell<TemporalPlanAction, String>> dragCellFactory
                = dragColumn.getCellFactory();
        dragColumn.cellFactoryProperty().setValue(tableColumn -> {
            TableCell<TemporalPlanAction, String> cell = dragCellFactory.call(tableColumn);
            cell.setStyle("-fx-alignment: CENTER;");
            return cell;
        });
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

        tableView.setRowFactory(view -> {
            TableRow<TemporalPlanAction> row = new TableRow<>();
            row.setOnDragDetected(tableDragDetected.apply(row));
            row.setOnDragOver(tableDragOver.apply(row));
            row.setOnDragDropped(event -> {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasContent(serializedMimeType)) {
                    int draggedIndex = (Integer) dragboard.getContent(serializedMimeType);
                    Action draggedAction = actionList.remove(draggedIndex);
                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = actionList.size();
                    } else {
                        dropIndex = row.getIndex();
                    }
                    logger.trace("Dropping action {} from row {} at index {}", draggedAction, draggedIndex, dropIndex);

                    actionList.add(dropIndex, draggedAction);
                    event.setDropCompleted(true);
                    event.consume();
                    updatePlan.accept(actionList, dropIndex);
                }
            });
            return row;
        });

        tableView.getColumns().setAll(dragColumn, actionColumn, whoColumn, whereColumn, whatColumn);
        tableView.getColumns().forEach(c -> c.setSortable(false));
        tableView.setEditable(false);
        return TableFilter.forTableView(tableView).lazy(true).apply();
    }

}
