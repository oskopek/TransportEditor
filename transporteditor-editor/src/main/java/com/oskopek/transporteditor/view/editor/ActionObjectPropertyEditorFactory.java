package com.oskopek.transporteditor.view.editor;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.RoadGraph;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.stream.Collectors;

public class ActionObjectPropertyEditorFactory extends DefaultPropertyEditorFactory {

    private RoadGraph graph;

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
