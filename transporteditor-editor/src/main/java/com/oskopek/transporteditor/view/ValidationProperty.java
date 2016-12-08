package com.oskopek.transporteditor.view;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import java.util.Arrays;

public class ValidationProperty extends SimpleBooleanProperty {

    private final Node[] nodes;
    private final String styleClass;
    private final Tooltip tooltip;
    private final String errorMessage;

    public ValidationProperty(String errorMessage, String styleClass, Node... nodes) {
        this.errorMessage = errorMessage;
        this.tooltip = new Tooltip(errorMessage);
        this.styleClass = styleClass;
        this.nodes = nodes;
    }

    public ValidationProperty(String errorMessage, Node... nodes) {
        this(errorMessage, "error", nodes);
    }

    public boolean isValid() {
        return get();
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        Arrays.stream(nodes).forEach(node -> {
            node.getStyleClass().removeAll(styleClass);
            if (isValid()) {
                Tooltip.uninstall(node, tooltip);
            } else {
                Tooltip.install(node, tooltip);
                node.getStyleClass().add(styleClass);
            }
        });
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
