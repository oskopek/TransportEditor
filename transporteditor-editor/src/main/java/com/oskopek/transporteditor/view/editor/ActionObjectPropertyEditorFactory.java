package com.oskopek.transporteditor.view.editor;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

public class ActionObjectPropertyEditorFactory extends DefaultPropertyEditorFactory {

    @Override
    public PropertyEditor<?> call(PropertySheet.Item item) {
//        if (LocationBuilder.class.isAssignableFrom(item.getType())) {
//            return new LocationPropertyEditor();
//        }
        return super.call(item);
    }
}
