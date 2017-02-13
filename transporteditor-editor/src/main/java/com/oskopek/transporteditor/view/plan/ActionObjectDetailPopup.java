package com.oskopek.transporteditor.view.plan;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.controlsfx.control.PopOver;

import java.util.Map;

public class ActionObjectDetailPopup extends PopOver {

    private final Map<String, String> info;

    public ActionObjectDetailPopup(Map<String, String> info) {
        this.info = info;
        regenerateBox();
        setAutoHide(false);
        setArrowLocation(ArrowLocation.BOTTOM_CENTER);
    }

    private void regenerateBox() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5d);
        gridPane.setVgap(5d);
        int row = 0;
        for (Map.Entry<String, String> entry : info.entrySet()) {
            Label keyNode = new Label(entry.getKey());
            keyNode.setStyle("-fx-text-fill: black;");
            StackPane keyPane = new StackPane(keyNode);

            Label valNode = new Label(entry.getValue());
            valNode.setStyle("-fx-text-fill: dimgrey;");
            StackPane valPane = new StackPane(valNode);

            gridPane.addRow(row, keyPane, valPane);
            gridPane.getRowConstraints().add(new RowConstraints(12));
            row++;
        }

        for (int i = 0; i < 2; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setFillWidth(true);
            columnConstraints.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        gridPane.setStyle("-fx-padding: 5px;");
        setContentNode(gridPane);
    }

    public ActionObjectDetailPopup putInfo(String key, String val) {
        info.put(key, val);
        regenerateBox();
        return this;
    }

    public ActionObjectDetailPopup putAllInfo(Map<String, String> map) {
        info.putAll(map);
        regenerateBox();
        return this;
    }


}
