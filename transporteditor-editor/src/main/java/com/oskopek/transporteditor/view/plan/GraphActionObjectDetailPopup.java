package com.oskopek.transporteditor.view.plan;

import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.*;

import java.util.Map;

public class GraphActionObjectDetailPopup extends PopupControl {

    private final Map<String, String> info;

    public GraphActionObjectDetailPopup(Map<String, String> info) {
        this.info = info;
        regenerateBox();
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

        gridPane.setStyle("-fx-background-color: gold; -fx-opacity: 0.9; -fx-border-width: 3px; -fx-border-insets: 0px;"
                + " -fx-border-style: solid; -fx-border-color: black; -fx-padding: 2px;");
        gridPane.applyCss();
        gridPane.layout();
        super.getContent().clear();
        super.getContent().add(gridPane);
    }

    public GraphActionObjectDetailPopup putInfo(String key, String val) {
        info.put(key, val);
        regenerateBox();
        return this;
    }

    public GraphActionObjectDetailPopup putAllInfo(Map<String, String> map) {
        info.putAll(map);
        regenerateBox();
        return this;
    }


}
