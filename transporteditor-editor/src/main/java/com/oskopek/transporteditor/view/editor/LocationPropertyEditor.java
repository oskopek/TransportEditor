package com.oskopek.transporteditor.view.editor;

import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.builder.ActionObjectBuilder;
import javafx.scene.Node;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.function.Consumer;

public class LocationPropertyEditor implements PropertyEditor<Location> {

    public LocationPropertyEditor(Consumer<ActionObjectBuilder<Location>> update) {

    }

    @Override
    public Node getEditor() {
        return null;
    }

    @Override
    public Location getValue() {
        return null;
    }

    @Override
    public void setValue(Location value) {

    }
}
