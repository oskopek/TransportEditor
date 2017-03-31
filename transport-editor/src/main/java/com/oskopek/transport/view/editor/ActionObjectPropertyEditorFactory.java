package com.oskopek.transport.view.editor;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.stream.Collectors;

/**
 * Custom {@link DefaultPropertyEditorFactory} implementation that handles our action objects correctly.
 * For example, creates custom and validated editors for {@link Location}s and {@link ActionCost}s.
 */
public class ActionObjectPropertyEditorFactory extends DefaultPropertyEditorFactory {

    private RoadGraph graph;

    /**
     * Default constructor.
     *
     * @param graph used for location validation
     */
    public ActionObjectPropertyEditorFactory(RoadGraph graph) {
        this.graph = graph;
    }

    @Override
    public PropertyEditor<?> call(PropertySheet.Item item) {
        if (Location.class.isAssignableFrom(item.getType())) {
            return Editors.createChoiceEditor(item, graph.getAllLocations().collect(Collectors.toList()));
        } else if (ActionCost.class.isAssignableFrom(item.getType()) && item.isEditable()) {
            return new ActionCostEditor(item);
        }
        return super.call(item);
    }
}
