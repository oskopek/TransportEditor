package com.oskopek.transporteditor.view.plan;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;

import java.util.SortedMap;

public class GraphActionObjectDetailPopup extends PopupWindow {

    private final SortedMap<String, String> info;

    public GraphActionObjectDetailPopup(SortedMap<String, String> info) {
        this.info = info;
        regenerateBox();
    }

    private void regenerateBox() {
        VBox infoBox = new VBox();
        info.forEach((key, val) -> {
            HBox hbox = new HBox();
            Label keyNode = new Label(key);
            Label valNode = new Label(val);
            hbox.getChildren().addAll(keyNode, valNode);
            infoBox.getChildren().add(hbox);
        });
        super.getContent().clear();
        super.getContent().add(infoBox);
    }

    public GraphActionObjectDetailPopup putInfo(String key, String val) {
        info.put(key, val);
        regenerateBox();
        return this;
    }

    public GraphActionObjectDetailPopup putAllInfo(SortedMap<String, String> map) {
        info.putAll(map);
        regenerateBox();
        return this;
    }


}
