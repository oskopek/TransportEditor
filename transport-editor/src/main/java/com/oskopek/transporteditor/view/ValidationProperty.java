package com.oskopek.transporteditor.view;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import java.util.Arrays;

/**
 * Boolean property extension which changes the style and tooltip of the associated nodes when false.
 */
public class ValidationProperty extends SimpleBooleanProperty {

    private final Node[] nodes;
    private final String styleClass;
    private final Tooltip tooltip;
    private final String errorMessage;

    /**
     * Default constructor.
     *
     * @param errorMessage the message to display in a tooltip when invalid
     * @param styleClass the style class to add when and only when invalid
     * @param nodes the nodes to validate and visualize on
     */
    public ValidationProperty(String errorMessage, String styleClass, Node... nodes) {
        this.errorMessage = errorMessage;
        this.tooltip = new Tooltip(errorMessage);
        this.styleClass = styleClass;
        this.nodes = nodes;
    }

    /**
     * Default constructor with style class "error".
     *
     * @param errorMessage the message to display in a tooltip when invalid
     * @param nodes the nodes to validate and visualize on
     */
    public ValidationProperty(String errorMessage, Node... nodes) {
        this(errorMessage, "error", nodes);
    }

    /**
     * Alias for {@link #get()} for more readable code.
     *
     * @return true iff {@link #get()} is true
     */
    public boolean isValid() {
        return get();
    }

    /**
     * Get the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
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

}
