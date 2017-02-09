package com.oskopek.transporteditor.view.editor;

import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.builder.ActionObjectBuilder;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.function.Consumer;

public abstract class ActionObjectPropertyEditor<T extends ActionObject> implements PropertyEditor<T> {


    private Consumer<ActionObjectBuilder<T>> updateCallback;

    public ActionObjectPropertyEditor(Consumer<ActionObjectBuilder<T>> updateCallback) {
        this.updateCallback = updateCallback;
    }


}
