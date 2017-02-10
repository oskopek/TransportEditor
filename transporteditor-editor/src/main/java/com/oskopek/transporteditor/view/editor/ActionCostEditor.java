package com.oskopek.transporteditor.view.editor;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

public class ActionCostEditor implements PropertyEditor<ActionCost> {

    private final Spinner<Integer> editor;

    public ActionCostEditor(PropertySheet.Item item) {
        this.editor = new Spinner<>();
        setValue((ActionCost) item.getValue());
    }

    @Override
    public Node getEditor() {
        return editor;
    }

    @Override
    public ActionCost getValue() {
        return ActionCost.valueOf(editor.getValue());
    }

    @Override
    public void setValue(ActionCost value) {
        editor.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE,
                value.getCost()));
    }
}
