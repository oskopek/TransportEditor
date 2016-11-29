package com.oskopek.transporteditor.view;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import java.util.Arrays;

public class ValidationProperty extends SimpleBooleanProperty {

    private final Node[] nodes;
    private final String styleClass;
    private final Tooltip tooltip;

    public ValidationProperty(String errorMessage, String styleClass, Node... nodes) {
        this.tooltip = new Tooltip(errorMessage);
        this.styleClass = styleClass;
        this.nodes = nodes;
    }

    public ValidationProperty(String errorMessage, Node... nodes) {
        this(errorMessage, "error", nodes);
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        Arrays.stream(nodes).forEach(node -> {
            Tooltip.install(node, tooltip);
            node.getStyleClass().removeAll("error");
            if (getValue()) {
                node.getStyleClass().add("error");
            }
        });
    }
}
